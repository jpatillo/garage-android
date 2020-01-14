package com.hygeiabiz.garage.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.hygeiabiz.garage.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject


class MainFragment : Fragment(),View.OnClickListener {

    private val _rcSIGNIN = 1002

    private var firebaseAuth = FirebaseAuth.getInstance()

    var token:String = ""

    //TODO facilitate adding a device
    val device = "34567"

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.main_fragment, container, false)
        val btn: Button = view.findViewById(R.id.message)
        val imageView: CircleImageView = view.findViewById(R.id.profile_image)
        btn.setOnClickListener(this)
        imageView.setOnClickListener(this)

        firebaseAuth.addAuthStateListener {
            if(it.currentUser != null){
                Picasso.get()
                    .load(it.currentUser?.photoUrl.toString())
                    .into(imageView)


                getUserToken()
            }
            else {
                token = ""
            }

        }

        return view
    }

    override fun onStart() {
        super.onStart()

        //TODO I think there is a situation in which the user has just been authenticated
        // and the token is already being retrieved when we get to this line.
        //This check is to make sure we have a valid token if the app is brought from the
        // background.
        if(firebaseAuth.currentUser!=null){
            getUserToken()
        }
    }

    private fun getUserToken() {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                token = it.result!!.token!!
                val queue = Volley.newRequestQueue(this.context)
                val url = "https://garage.jamespatillo.com/u/" + firebaseAuth.currentUser?.uid
                val jsonBody:JSONObject  = JSONObject("{id:${firebaseAuth.currentUser?.uid}," +
                        "name:'${firebaseAuth.currentUser?.displayName}'," +
                        "email:${firebaseAuth.currentUser?.email}," +
                        "photo:'${firebaseAuth.currentUser?.photoUrl}'}");

                // Request a json response from the provided URL.
                val jsonObjectRequest  = object: JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    Response.Listener { response ->
                        //TODO server response not yet useful
                        Log.d("server says: ", response.toString())
                    },
                    Response.ErrorListener {
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(this.context, "Error!", duration)
                        toast.show()
                    })
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer $token"
                        return headers
                    }
                }

                // Add the request to the RequestQueue.
                queue.add(jsonObjectRequest)


            } else { // Handle error -> task.getException();
            }

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_image -> {

                if(firebaseAuth.currentUser==null) {
                    // Choose authentication providers
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )

                    // Create and launch sign-in intent
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                        _rcSIGNIN
                    )
                } else {
                    firebaseAuth.signOut()
                }
            }
            R.id.message -> {

                if(token=="")return

                val queue = Volley.newRequestQueue(this.context)
                val url = "https://garage.jamespatillo.com/garage/${device}/door?u=${firebaseAuth.currentUser?.uid}"

                // Request a string response from the provided URL.
                val stringRequest = object: StringRequest(
                    Request.Method.GET, url,
                    Response.Listener<String> { response ->
                        //TODO server response not yet useful
                        Log.d("server says: ", response)
                    },
                    Response.ErrorListener {
                        val duration = Toast.LENGTH_SHORT
                        val toast = Toast.makeText(this.context, "Error!", duration)
                        toast.show()
                    })
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer $token"
                        return headers
                    }
                }

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
            }

            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == _rcSIGNIN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }


}

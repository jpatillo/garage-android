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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.hygeiabiz.garage.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class MainFragment : Fragment(),View.OnClickListener {

    val RC_SIGN_IN = 1002

    var firebaseAuth = FirebaseAuth.getInstance()

    var token:String = ""

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

                it.currentUser?.getIdToken(true)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        token = it.result!!.token!!
                    } else { // Handle error -> task.getException();
                    }

                }
            }
        }


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_image -> {
                // Choose authentication providers
                val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build())

                // Create and launch sign-in intent
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN)
            }
            R.id.message -> {

                if(token=="")return

                val queue = Volley.newRequestQueue(this.context)
                val url = "https://garage.jamespatillo.com/garage/open"

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

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                Log.d("----user----",user?.photoUrl.toString())


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

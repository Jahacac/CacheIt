package com.example.cacheit.authenticationActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.cacheit.Firebase
import com.example.cacheit.R

class VerifyEmailActivity : AppCompatActivity() {
    private val tag = "VerifyEmailActivity"

    //UI elements
    private var btnVerifyEmail: Button? = null
    private var btnResendEmail: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        initialise()
    }

    private fun initialise() {
        btnVerifyEmail = findViewById<View>(R.id.btn_verify_email) as Button
        btnResendEmail = findViewById<View>(R.id.btn_resend_email) as Button

        btnVerifyEmail!!.setOnClickListener {
            val currentUserAuth = Firebase.auth!!.currentUser!!.reload()
            currentUserAuth.addOnSuccessListener {
                val user = Firebase.auth!!.currentUser
                if(user?.isEmailVerified!!) {
                    /*val intent = Intent(this@VerifyEmailActivity, MapsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)*/
                }
                else {
                    Log.d(tag, "Email not verified.")
                    Toast.makeText(this, "Email not verified.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnResendEmail!!.setOnClickListener{
            val mUser = Firebase.auth!!.currentUser
            mUser!!.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@VerifyEmailActivity,
                            "Verification email sent to " + mUser.email,
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(tag, "sendEmailVerification", task.exception)
                        Toast.makeText(this@VerifyEmailActivity,
                            "Failed to send verification email.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

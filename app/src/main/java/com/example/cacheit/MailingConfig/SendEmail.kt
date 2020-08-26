package com.example.cacheit.MailingConfig

import android.os.AsyncTask
import android.os.StrictMode
import android.util.Log
import com.example.cacheit.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File


class SendEmail(subject : String, body: String, sender : String, recipients : String, attachment : File) : AsyncTask<Void, Void, String>() {
    var subject = subject
    var message = body
    var mDatabase: FirebaseDatabase? = null
    var mAuth: FirebaseAuth? = null
    var file = attachment
    var recipient = recipients

    override fun doInBackground(vararg params: Void?): String? {
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)
        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val sender = GMailSender(
            "cacheitapp@gmail.com",
            "sandiboss123"
        )
        sender.sendMail(subject, message, "cacheitapp@gmail.com", recipient, file)
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        // ...
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // ...
    }
}
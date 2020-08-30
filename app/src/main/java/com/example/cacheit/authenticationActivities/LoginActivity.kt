package com.example.cacheit.authenticationActivities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cacheit.Firebase
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.example.cacheit.mainActivities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val tag = "LoginActivity"

    //global variables
    private var email: String? = null
    private var password: String? = null
    private var showPasswordFlag: Boolean = false

    //UI elements
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvForgotPassword: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var tvSignUpHere: TextView? = null

    //Firebase references
    private var mAuth: FirebaseAuth? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mFirestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initialise()
    }

    private fun initialise() {
        etEmail = findViewById<View>(R.id.email) as EditText
        etPassword = findViewById<View>(R.id.password) as EditText
        btnLogin = findViewById<View>(R.id.btn_login) as Button
        tvForgotPassword = findViewById<View>(R.id.forgot_password) as TextView
        mProgressBar = findViewById<View>(R.id.login_progress_bar) as ProgressBar
        tvSignUpHere = findViewById<View>(R.id.tv_sign_up) as TextView

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mFirestore = FirebaseFirestore.getInstance()

        etPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etPassword!!.text.isNotEmpty()) {
                    btn_show_password.visibility = View.VISIBLE
                }
                else {
                    btn_show_password.visibility = View.GONE
                }
            }

        })

        btn_show_password.setOnClickListener {
            showPasswordFlag = !showPasswordFlag
            if(showPasswordFlag) {
                etPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password.setImageResource(R.drawable.ic_eye)
            } else {
                etPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password.setImageResource(R.drawable.ic_eye_signup)
            }
            etPassword!!.typeface = tv_sign_up.typeface
            etPassword!!.setSelection(etPassword!!.text.length)
        }

        tvForgotPassword!!
            .setOnClickListener { startActivity(
                Intent(this@LoginActivity,
                    ForgotPasswordActivity::class.java)
            ) }

        btnLogin!!.setOnClickListener { loginUser() }

        tvSignUpHere!!
            .setOnClickListener { startActivity(
                Intent(this@LoginActivity,
                    CreateAccountActivity::class.java)
            ) }
    }

    private fun loginUser() {
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()
        val hideKeyboard = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        hideKeyboard?.hideSoftInputFromWindow(btnLogin?.windowToken, 0)


        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar?.visibility = View.VISIBLE
            Log.d(tag, "Logging in user.")

            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with signed-in user's information
                        Log.d(tag, "signInWithEmail:success")
                        val ref = mDatabase!!.getReference("/Users/${mAuth?.uid}")
                        ref.child("email").setValue(email)

                        //save token id to Firestore
                        val tokenId: Any? = FirebaseInstanceId.getInstance().getToken()
                        val currentId = mAuth!!.currentUser?.uid

                        val tokenMap: MutableMap<String, Any?> = HashMap()
                        tokenMap["token_id"] = tokenId

                        if (currentId != null) {
                            mFirestore
                                ?.collection("Users")
                                ?.document(currentId)
                                ?.update(tokenMap)
                                ?.addOnSuccessListener {
                                    Log.d(tag, "TokenId saved to Firestore")
                                }
                            Firebase.databaseUsers
                                ?.child(currentId)
                                ?.child("tokenId")
                                ?.setValue(tokenId)
                        }

                        updateUI()
                    } else {
                        // If sign in fails, display a message to the user.
                        mProgressBar?.visibility = View.GONE
                        Log.e(tag, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        val intent = if(mAuth!!.currentUser?.isEmailVerified!!) {
            Intent(this@LoginActivity, MainActivity::class.java)
        } else {
            Intent(this@LoginActivity, VerifyEmailActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}

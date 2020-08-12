package com.example.cacheit.authenticationActivities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cacheit.Firebase
import com.example.cacheit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountActivity : AppCompatActivity() {
    private val tag = "CreateAccountActivity"

    //global variables
    private var email: String? = null
    private var fullName: String? = null
    private var username: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null
    private var showPasswordFlag: Boolean = false
    private var showConfirmPasswordFlag: Boolean = false

    //UI elements
    private var etEmail: EditText? = null
    private var etFullName: EditText? = null
    private var etUsername: EditText? = null
    private var etPassword: EditText? = null
    private var etConfirmPassword: EditText? = null
    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressBar? = null
    private var tvSignInHere: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initialise()
    }

    private fun initialise() {
        etEmail = findViewById<View>(R.id.email) as EditText
        etFullName = findViewById<View>(R.id.etFullName) as EditText
        etUsername = findViewById<View>(R.id.etUsername) as EditText
        etPassword = findViewById<View>(R.id.password) as EditText
        etConfirmPassword = findViewById<View>(R.id.etConfirmPassword) as EditText
        btnCreateAccount = findViewById<View>(R.id.btn_register) as Button
        mProgressBar = findViewById<View>(R.id.login_progress_bar) as ProgressBar
        tvSignInHere = findViewById<View>(R.id.signInHere) as TextView

        etPassword!!.setOnClickListener {
            etPassword!!.error = null
            btn_show_password.visibility = View.VISIBLE
        }

        etConfirmPassword!!.setOnClickListener {
            etConfirmPassword!!.error = null
            btn_show_confirm_password.visibility = View.VISIBLE
        }

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

        etConfirmPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etConfirmPassword!!.text.isNotEmpty()) {
                    btn_show_confirm_password.visibility = View.VISIBLE
                }
                else {
                    btn_show_confirm_password.visibility = View.GONE
                }
            }

        })

        btn_show_password.setOnClickListener {
            showPasswordFlag = !showPasswordFlag
            if(showPasswordFlag) {
                etPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password.setImageResource(R.drawable.ic_hidden_eye)
            } else {
                etPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password.setImageResource(R.drawable.ic_eye)
            }
            etPassword!!.typeface = signInHere.typeface
            etPassword!!.setSelection(etPassword!!.text.length)
        }

        btn_show_confirm_password.setOnClickListener {
            showConfirmPasswordFlag = !showConfirmPasswordFlag
            if(showConfirmPasswordFlag) {
                etConfirmPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_confirm_password.setImageResource(R.drawable.ic_hidden_eye)
            } else {
                etConfirmPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_confirm_password.setImageResource(R.drawable.ic_eye)
            }
            etConfirmPassword!!.typeface = signInHere.typeface
            etConfirmPassword!!.setSelection(etConfirmPassword!!.text.length)
        }

        etUsername!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                username = etUsername?.text.toString()

                val usernameDb = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(username)
                usernameDb.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()) {
                            etUsername?.error = "Username already exists"
                            etUsername?.requestFocus()
                        }
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e(tag, "Failed to read value. " + p0.message)
                    }
                })
            }
        })

        btnCreateAccount!!.setOnClickListener { createNewAccount() }

        tvSignInHere!!
            .setOnClickListener { startActivity(
                Intent(this@CreateAccountActivity,
                    LoginActivity::class.java)
            ) }
    }

    private fun createNewAccount() {
        email = etEmail?.text.toString()
        fullName = etFullName?.text.toString()
        username = etUsername?.text.toString()
        password = etPassword?.text.toString()
        confirmPassword = etConfirmPassword?.text.toString()


        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show()
            etPassword?.requestFocus()
            btn_show_password.visibility = View.GONE
        }
        else if(!password.equals(confirmPassword)) {
            etConfirmPassword?.error = "Passwords don't match"
            etConfirmPassword?.requestFocus()
            btn_show_confirm_password.visibility = View.GONE
        }
        else {
            if(etUsername?.error == "Username already exists") {
                Toast.makeText(this, "Invalid username!", Toast.LENGTH_SHORT).show()
            }
            else {
                mProgressBar?.visibility = View.VISIBLE

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        mProgressBar?.visibility = View.INVISIBLE

                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "createUserWithEmail:success")

                            val userId = Firebase.auth!!.currentUser!!.uid

                            //Verify Email
                            verifyEmail()

                            //update user profile information
                            val currentUserDb = Firebase.databaseUsers!!.child(userId)
                            val user = User(
                                userId,
                                email!!,
                                fullName!!,
                                username!!,
                                "https://firebasestorage.googleapis.com/v0/b/cacheit-759ee.appspot.com/o/images%2Fblank_profile_photo.jpeg?alt=media&token=21494e7f-acf7-411f-86ad-70459f8a1ee7"
                            )
                            currentUserDb.setValue(user).addOnSuccessListener {
                                updateUserInfoAndUI()
                            }

                            Toast.makeText(this@CreateAccountActivity, "Account created!", Toast.LENGTH_SHORT).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this@CreateAccountActivity, (task.exception?.message
                                ?: "Authentication failed."), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun updateUserInfoAndUI() {
        //save token id to Firestore
        val tokenId: Any? = FirebaseInstanceId.getInstance().getToken()
        val currentId = Firebase.auth!!.currentUser?.uid

        val tokenMap: MutableMap<String, Any?> = HashMap()
        tokenMap["token_id"] = tokenId
        tokenMap["fullName"] = fullName
        tokenMap["username"] = username

        if (currentId != null) {
            Firebase.firestore
                ?.collection("Users")
                ?.document(currentId)
                ?.set(tokenMap)
                ?.addOnSuccessListener {
                    Log.d(tag, "TokenId saved to Firestore")
                }
            Firebase.databaseUsers
                ?.child(currentId)
                ?.child("tokenId")
                ?.setValue(tokenId)
        }

        //start next activity
        val intent = Intent(this@CreateAccountActivity, VerifyEmailActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun verifyEmail() {
        val mUser = Firebase.auth!!.currentUser
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity,
                        "Verification email sent to " + mUser.email,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(tag, "sendEmailVerification", task.exception)
                    Toast.makeText(this@CreateAccountActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}

class User(val id: String, val email: String, val fullName: String, val username: String, val profilePhotoUrl: String)


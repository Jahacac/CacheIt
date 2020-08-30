package com.example.cacheit.userProfileActivities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cacheit.Firebase
import com.example.cacheit.R
import com.example.cacheit.authenticationActivities.LoginActivity
import com.example.cacheit.gamesActivities.GamesData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_create_account.*
import java.util.*
import kotlin.concurrent.schedule


class UserProfileSettingsActivity : Fragment()  {

    private var root: View? = null
    private var imageUri: Uri? = null
    private var user: UserCard? = null

    // Logged in user ID
    private var uid: String? = null
    private var initialUsername: String? = null
    private var initialEmail: String? = null
    private var initialPhotoUri: String? = null

    //UI elements
    private var imgProfilePicture: CircleImageView? = null
    private var etUsername: EditText? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var pbSaveProfile: ProgressBar? = null
    private var btnSave: Button? = null
    private var btnClose: ImageButton? = null
    private var btnSelectGamePicture: Button? = null
    private var btn_show_password: ImageButton? = null
    private var showPasswordFlag: Boolean = false

    //  Firebase references
    private var mAuth: FirebaseAuth? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_user_settings, container, false)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users/")
        uid = mAuth!!.uid

        btnSave = root?.findViewById<View>(R.id.btn_register) as Button
        btnClose = root?.findViewById<View>(R.id.btn_close) as ImageButton
        etEmail = root?.findViewById<View>(R.id.email) as EditText
        etUsername = root?.findViewById<View>(R.id.etUsername) as EditText
        etPassword = root?.findViewById<View>(R.id.password) as EditText
        imgProfilePicture = root?.findViewById<View>(R.id.img_game_photo) as CircleImageView
        btnSelectGamePicture = root?.findViewById<View>(R.id.btn_select_game_photo) as Button
        btn_show_password = root?.findViewById<View>(R.id.btn_show_password) as ImageButton
        pbSaveProfile = root?.findViewById<View>(R.id.login_progress_bar) as ProgressBar

        val mFirebaseAuth = FirebaseAuth.getInstance()

        setEditTextValues()

        btnClose!!.setOnClickListener {
            exitSettingsGame()
        }

        btnSelectGamePicture!!.setOnClickListener {
            Log.d(tag, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_UPLOAD_REQUEST_CODE)
        }

        btnSave!!.setOnClickListener {
            Log.d(tag, "Save user profile changes")
            saveChanges()
        }

        return root
    }

    private fun setEditTextValues() {
        val userRef = mDatabaseReference!!.child("/$uid")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                initialUsername = p0.child("username").value.toString()
                initialEmail = p0.child("email").value.toString()
                etUsername!!.setText(initialUsername)
                etEmail!!.setText(initialEmail)
                initialPhotoUri = p0.child("profilePhotoUrl").value.toString()

                Glide.with(this@UserProfileSettingsActivity)
                    .load(initialPhotoUri)
                    .into(imgProfilePicture!!)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(tag, p0.message)
            }
        })

        etPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etPassword!!.text.isNotEmpty()) {
                    btn_show_password?.visibility = View.VISIBLE
                }
                else {
                    btn_show_password?.visibility = View.GONE
                }
            }
        })

        btn_show_password?.setOnClickListener {
            showPasswordFlag = !showPasswordFlag
            if(showPasswordFlag) {
                etPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password!!.setImageResource(R.drawable.ic_eye)
            } else {
                etPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password!!.setImageResource(R.drawable.ic_eye_signup)
            }
        }

        // Check if username already exists in database
        etUsername!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var username = etUsername?.text.toString()
                if (initialUsername != username) {
                    val usernameDb = mDatabaseReference!!.orderByChild("username").equalTo(username)
                    usernameDb.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                etUsername?.error = "Username already exists"
                                etUsername?.requestFocus()
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            Log.e(tag, "Failed to read value. " + p0.message)
                        }
                    })
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_UPLOAD_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(tag, "Photo was selected")

            // location where selected image is stored on the device
            imgProfilePicture?.setImageURI(data.data)
            imageUri = data.data
        }
    }

    private fun saveChanges() {
        if (etUsername?.error == "Username already exists") {
            Toast.makeText(activity, "Invalid username!", Toast.LENGTH_SHORT).show()
            return
        }

        val username = etUsername?.text.toString()
        val email = etEmail?.text.toString()
        val password = etPassword?.text.toString()

        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Please enter all details!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(activity, "Please enter a valid E-mail address!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(username)) {
            Toast.makeText(activity, "Please enter a username!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Please enter your password!", Toast.LENGTH_SHORT)
                .show()
        } else {

            val user = mAuth?.currentUser
            if(user != null) {
                val credential = EmailAuthProvider.getCredential(initialEmail!!, password)
                user.reauthenticate(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (etEmail?.text.toString() != initialEmail) {
                            val dialogBuilder = AlertDialog.Builder(activity)
                            dialogBuilder.setMessage("If you change your E-mail address, you will be logged out and required to verify your new E-mail address. Would you like to continue?")
                                // if the dialog is cancelable
                                .setCancelable(true)
                                .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                                    btnSave?.visibility = View.INVISIBLE
                                    pbSaveProfile?.visibility = View.VISIBLE

                                    val ref = mDatabase!!.getReference("/Users/$uid")
                                    ref?.child("email")?.setValue(email)
                                    ref?.child("username")?.setValue(username)

                                    if (imageUri == null) {
                                        ref?.child("profilePhotoUrl")
                                            ?.setValue("https://firebasestorage.googleapis.com/v0/b/cacheit-759ee.appspot.com/o/images%2Fblank_profile_photo.jpeg?alt=media&token=21494e7f-acf7-411f-86ad-70459f8a1ee7")
                                    } else {
                                        uploadProfilePhotoToFirebaseStorage()
                                    }

                                    val mUser = Firebase.auth!!.currentUser
                                    val dialogBuilder = AlertDialog.Builder(requireActivity())
                                    dialogBuilder.setMessage("You will now be signed out, check your new E-mail for a verification link.")
                                        // if the dialog is cancelable
                                        .setCancelable(true)
                                        .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                                            mUser!!.updateEmail(email)
                                            val tokenMap: MutableMap<String, Any> = HashMap()
                                            tokenMap["token_id"] = ""

                                            var mUser = Firebase.auth!!.currentUser
                                            mUser!!.sendEmailVerification()
                                                .addOnCompleteListener(requireActivity()) { task ->
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(activity,
                                                            "Verification email sent to " + mUser.email,
                                                            Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Log.e(tag, "sendEmailVerification", task.exception)
                                                        Toast.makeText(activity,
                                                            "Failed to send verification email.",
                                                            Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                            Firebase.firestore?.collection("Users")
                                                ?.document(Firebase.userId)?.update(tokenMap)
                                            Firebase.databaseUsers?.child(Firebase.userId)
                                                ?.child("tokenId")?.removeValue()
                                            Firebase.auth?.signOut()
                                            val intent =
                                                Intent(activity, LoginActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                        }

                                    val alert = dialogBuilder.create()
                                    alert.show()

                                }
                                .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                                    return@setNegativeButton
                                }

                            val alert = dialogBuilder.create()
                            alert.show()
                        } else {
                            btnSave?.visibility = View.INVISIBLE
                            pbSaveProfile?.visibility = View.VISIBLE
                            val ref = mDatabase!!.getReference("/Users/$uid")
                            ref?.child("username")?.setValue(username)

                            if (imageUri == null) {
                                ref?.child("profilePhotoUrl")
                                    ?.setValue("https://firebasestorage.googleapis.com/v0/b/cacheit-759ee.appspot.com/o/images%2Fblank_profile_photo.jpeg?alt=media&token=21494e7f-acf7-411f-86ad-70459f8a1ee7")

                                Toast.makeText(activity, "Profile successfully updated!", Toast.LENGTH_SHORT)
                                    .show()

                                Timer("SettingUp", false).schedule(1000) {
                                    val myProfileFragment = UserProfileActivity.newInstance()
                                    childFragmentManager.beginTransaction().replace(R.id.create_game_main,myProfileFragment).commit()
                                }


                            } else {
                                uploadProfilePhotoToFirebaseStorage()
                            }
                        }
                    } else {
                        Toast.makeText(activity, "Incorrect Password.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun uploadProfilePhotoToFirebaseStorage() {
        val refStorage = FirebaseStorage.getInstance().getReference("/images/user/${uid}")
        imageUri.let { it ->
            if (it != null) {
                refStorage.putFile(it)
                    .addOnSuccessListener { task ->
                        Log.d(tag, "Successfully uploaded image: ${task.metadata?.path}")

                        refStorage.downloadUrl.addOnSuccessListener {
                            Log.d(tag, "File location: $it")

                            saveProfilePhotoToFirebaseDatabase(it.toString())
                        }
                    }
                    .addOnFailureListener {
                        Log.d(tag, it.message.toString())
                        exitSettingsGame()
                    }
            }
        }
    }

    private fun saveProfilePhotoToFirebaseDatabase(profileImageUrl: String) {
        val ref = mDatabase!!.getReference("/Users/$uid")
        ref.child("profilePhotoUrl").setValue(profileImageUrl)
            .addOnSuccessListener {
                Log.d(tag, "Saved profile photo to firebase database")
            }
            .addOnFailureListener {
                Log.d(tag, "Failed to save profile photo firebase database")
            }
        Toast.makeText(activity, "Profile successfully updated!", Toast.LENGTH_SHORT)
            .show()

        Timer("SettingUp", false).schedule(1000) {
            val myProfileFragment = UserProfileActivity.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.create_game_main,myProfileFragment).commit()
        }
    }

    private fun exitSettingsGame() {
        val changesFlag = (initialEmail != etEmail?.text.toString() || initialUsername != etUsername?.text.toString())
        Log.e("changesflag", changesFlag.toString())
        if(changesFlag) {
            Log.e("changesflag", "true")

            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage("You have unsaved data, are you sure?")
                // if the dialog is cancelable
                .setCancelable(true)
                .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                    val myProfileFragment = UserProfileActivity.newInstance()
                    childFragmentManager.beginTransaction().replace(R.id.create_game_main,myProfileFragment).commit()
                }

            val alert = dialogBuilder.create()
            alert.show()
        } else {
            Log.e("changesflag", "false")
            val myProfileFragment = UserProfileActivity.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.create_game_main,myProfileFragment).commit()
        }
    }

    companion object {
        private const val IMAGE_UPLOAD_REQUEST_CODE = 50000
        fun newInstance(): UserProfileSettingsActivity = UserProfileSettingsActivity()
    }

}
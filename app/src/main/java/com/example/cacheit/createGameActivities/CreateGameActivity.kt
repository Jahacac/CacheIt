package com.example.cacheit.createGameActivities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.cacheit.Firebase
import com.example.cacheit.R
import com.example.cacheit.myGamesActivities.MyGamesActivity
import com.google.android.gms.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_create_game.select_button
import kotlinx.android.synthetic.main.activity_select_location_dialog.view.*
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

//import kotlinx.android.synthetic.main.activity_create_game.mapView;


class CreateGameActivity : Fragment()  {

    private var root: View? = null
    private var imageUri: Uri? = null

    // current user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    // Logged in user ID
    private var uid: String? = null
    private val gameId = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT)

    //UI elements
    private var mapView: MapView? = null
    private var btnSelectLocation: Button? = null
    private var btnCreateGame: Button? = null
    private var etName: EditText? = null
    private var rbDifficulty: RadioGroup? = null
    private var etLat: EditText? = null
    private var etLon: EditText? = null
    private var etHint: EditText? = null
    private var btnSelectGamePicture: Button? = null
    private var imgGamePicture: CircleImageView? = null
    private var pbSaveGame: ProgressBar? = null
    private var cbLocationHint: CheckBox? = null
    private var btnRefresh: ImageButton? = null
    private var btnClose: ImageButton? = null

    //  Firebase references
    private var mAuth: FirebaseAuth? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // initialize FusedLocationProviderClient
        fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        getLastLocation()
        Log.e("lon", currentLat.toString())
        Log.e("lat", currentLon.toString())
        root = inflater.inflate(R.layout.activity_create_game, container, false)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        uid = mAuth!!.uid

        btnSelectLocation = root?.findViewById<View>(R.id.select_button) as Button
        btnCreateGame = root?.findViewById<View>(R.id.create_game) as Button
        etName = root?.findViewById<View>(R.id.et_name) as EditText
        etLat = root?.findViewById<View>(R.id.et_lat) as EditText
        etLon = root?.findViewById<View>(R.id.et_lon) as EditText
        etHint = root?.findViewById<View>(R.id.et_hint) as EditText
        rbDifficulty = root?.findViewById<View>(R.id.radio_difficulty) as RadioGroup
        btnSelectGamePicture = root?.findViewById<View>(R.id.btn_select_game_photo) as Button
        imgGamePicture = root?.findViewById<View>(R.id.img_game_photo) as CircleImageView
        pbSaveGame = root?.findViewById<View>(R.id.pb_save_changes) as ProgressBar
        cbLocationHint = root?.findViewById<CheckBox>(R.id.cb_location_hint) as CheckBox
        btnRefresh = root?.findViewById<ImageButton>(R.id.btn_refresh) as ImageButton
        btnClose = root?.findViewById<ImageButton>(R.id.btn_close) as ImageButton

        btnSelectLocation!!.setOnClickListener {
            //initialise the map
            activity?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
            mapView?.onCreate(savedInstanceState)
            mapView?.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                    // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                }
            }

            val intent = PlacePicker.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlacePickerOptions.builder()
                        .statingCameraPosition(
                            CameraPosition.Builder()
                                .target(currentLat?.let { it1 -> currentLon?.let { it2 ->
                                    LatLng(it1,
                                        it2
                                    )
                                } })
                                .zoom(16.0)
                                .build())
                        .build())
                .build(activity)
            startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE)
        }

        btnSelectGamePicture!!.setOnClickListener {
            Log.d(tag, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_UPLOAD_REQUEST_CODE)
        }

        btnCreateGame!!.setOnClickListener {
            Log.d(tag, "Create game")
            createGame()
        }

        btnRefresh!!.setOnClickListener {
            Log.d(tag, "Refresh create game")
            refreshCreateGame()
        }

        btnClose!!.setOnClickListener {
            Log.d(tag, "Close game creator")
            exitCreateGame()
        }
        return root
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            activity?.let {
                fusedLocationClient.lastLocation.addOnCompleteListener(it) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLat = location.latitude
                        currentLon = location.longitude
                    }
                }
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLat = mLastLocation.latitude
            currentLon = mLastLocation.longitude
        }
    }

    private fun checkPermissions(): Boolean {
        if (activity?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } == PackageManager.PERMISSION_GRANTED &&
            activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_SELECTION_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Retrieve the information from the selected location's CarmenFeature
            var carmenFeature: CarmenFeature? = PlacePicker.getPlace(data)
            if (carmenFeature != null) {
                var geo = carmenFeature.geometry().toString()

                var lon = geo.substringAfter("coordinates=[").substringBefore(',')
                var lat = geo.substringAfter("$lon, ").substringBefore(']')

                etLat?.setText(lat)
                etLon?.setText(lon)
            }
        }

        if (requestCode == IMAGE_UPLOAD_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(tag, "Photo was selected")

            // location where selected image is stored on the device
            imgGamePicture?.setImageURI(data.data)
            imageUri = data.data
        }
    }

    private fun createGame() {
        val gameName = etName?.text.toString()
        val gameHint = etHint?.text.toString()
        val gameLat = etLat?.text.toString()
        val gameLon = etLon?.text.toString()

        if (TextUtils.isEmpty(gameName) && TextUtils.isEmpty(gameLat) && TextUtils.isEmpty(gameLon) && TextUtils.isEmpty(gameHint)) {
            Toast.makeText(activity, "Please enter all details!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(gameName)) {
            Toast.makeText(activity, "Please enter the game name!", Toast.LENGTH_SHORT).show()
        } else if (etLat?.text.toString().isEmpty() || etLon?.text.toString().isEmpty()) {
            Toast.makeText(activity, "Please select or enter the location!", Toast.LENGTH_SHORT)
                .show()
        } else if (etHint?.text.toString().isEmpty()) {
            Toast.makeText(activity, "Please enter a hint!", Toast.LENGTH_SHORT)
                .show()
        } else {
            pbSaveGame?.visibility = View.VISIBLE

            val radioId = rbDifficulty?.checkedRadioButtonId
            val rbSelected: RadioButton? = radioId?.let { root?.findViewById<RadioButton>(it) } as RadioButton
            val gameDifficulty = rbSelected?.text.toString()

            val dbGame = Firebase.databaseGames?.child(gameId)
            dbGame?.push()
            dbGame?.child("name")?.setValue(gameName)
            dbGame?.child("id")?.setValue(gameId)
            dbGame?.child("ownerId")?.setValue(uid)
            dbGame?.child("difficulty")?.setValue(gameDifficulty)
            dbGame?.child("rating")?.setValue(0)
            dbGame?.child("ratingCount")?.setValue(0)
            dbGame?.child("locationHint")?.setValue(cbLocationHint?.isChecked)
            dbGame?.child("hint")?.setValue(gameHint)
            dbGame?.child("lat")?.setValue(gameLat)
            dbGame?.child("lon")?.setValue(gameLon)

            if (imageUri == null) {
                dbGame?.child("gameImg")
                    ?.setValue("https://firebasestorage.googleapis.com/v0/b/cacheit-759ee.appspot.com/o/images%2Fblank_profile_photo.jpeg?alt=media&token=21494e7f-acf7-411f-86ad-70459f8a1ee7")
                refreshCreateGame()
                Toast.makeText(activity, "Game successfully created!", Toast.LENGTH_SHORT).show()
            } else {
                uploadProfilePhotoToFirebaseStorage()
            }
            btnCreateGame?.visibility = View.INVISIBLE
            pbSaveGame?.visibility = View.VISIBLE
        }
    }

    private fun uploadProfilePhotoToFirebaseStorage() {
        val refStorage =
            FirebaseStorage.getInstance().getReference("/images/game_photos/${gameId}")
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
                    }
            }
        }
    }

    private fun saveProfilePhotoToFirebaseDatabase(profileImageUrl: String) {
        val ref = mDatabase!!.getReference("/Games/$gameId")
        ref.child("gameImg").setValue(profileImageUrl)
            .addOnSuccessListener {
                Log.d(tag, "Saved profile photo to firebase database")
                refreshCreateGame()
                Toast.makeText(activity, "Game successfully created!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d(tag, "Failed to save profile photo firebase database")
            }
    }

    private fun refreshCreateGame() {
        etName?.setText("")
        etHint?.setText("")
        etLat?.setText("")
        etLon?.setText("")
        cbLocationHint?.isChecked = false
        var b = root?.findViewById<RadioButton>(R.id.easy) as RadioButton;
        b.isChecked = true;
        btnCreateGame?.visibility = View.VISIBLE
        pbSaveGame?.visibility = View.INVISIBLE
        imgGamePicture?.setImageURI(null)
    }

    private fun exitCreateGame() {
        Log.e("exitcreategame", "entered function")
        val gameName = etName?.text.toString()
        val gameHint = etHint?.text.toString()
        val gameLat = etLat?.text.toString()
        val gameLon = etLon?.text.toString()

        val changesFlag = (!TextUtils.isEmpty(gameName) || !TextUtils.isEmpty(gameLat) || !TextUtils.isEmpty(gameLon) || !TextUtils.isEmpty(gameHint))
        Log.e("changesflag", changesFlag.toString())
        if(changesFlag) {
            Log.e("changesflag", "true")

            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage("You have unsaved data, are you sure?")
                // if the dialog is cancelable
                .setCancelable(true)
                .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                    val myGamesFragment = MyGamesActivity.newInstance()
                    childFragmentManager.beginTransaction().replace(R.id.create_game_main,myGamesFragment).commit()
                }

            val alert = dialogBuilder.create()
            alert.show()
        } else {
            Log.e("changesflag", "false")
            val myGamesFragment = MyGamesActivity.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.create_game_main,myGamesFragment).commit()
        }
    }

    companion object {
        private const val PLACE_SELECTION_REQUEST_CODE = 56789
        private const val IMAGE_UPLOAD_REQUEST_CODE = 50000
        private const val PERMISSION_ID = 42
        fun newInstance(): CreateGameActivity = CreateGameActivity()
    }

}
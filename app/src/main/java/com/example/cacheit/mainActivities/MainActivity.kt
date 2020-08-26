package com.example.cacheit.mainActivities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cacheit.*
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.example.cacheit.gameplayActivities.GameplayCard
import com.example.cacheit.gameplayActivities.GameplayData
import com.example.cacheit.gameplayActivities.MyGameplayActivity
import com.example.cacheit.gamesActivities.AllGamesActivity
import com.example.cacheit.gamesActivities.GameCard
import com.example.cacheit.gamesActivities.GamesData
import com.example.cacheit.myGamesActivities.MyGamesActivity
import com.example.cacheit.shared.GeofenceBroadcastReceiver
import com.example.cacheit.shared.GeofenceHelper
import com.example.cacheit.shared.LocationActivites.Companion.distance
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoQuery
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class MainActivity : AppCompatActivity() {

    lateinit var toolbar: ActionBar
    lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper

    // current user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private var GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private var FINE_LOCATION_ACCESS_REQUEST_CODE: Int = 10001;
    private var BACKGROUND_LOCATION_ACCESS_REQUEST_CODE: Int = 10002;

    companion object {
        var activeGameplay = false;
        var mainContext = this;
        internal const val ACTION_GEOFENCE_EVENT = "MainActivity.cacheit.action.ACTION_GEOFENCE_EVENT"
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        foregroundAndBackgroundLocationPermissionApproved()

        // initialize FusedLocationProviderClient
        fusedLocationClient = this.let { LocationServices.getFusedLocationProviderClient(it) }!!
        getLastLocation()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelper = GeofenceHelper(this);

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.navigation_games;
        GameplayData.fetchMyActiveGameplayData(object : MyActiveGameplayDataCallback {
            override fun onMyActiveGameplayDataCallback(myGameplayData: GameplayCard) {
                Log.e("fetching gameplay", myGameplayData.toString())
                if (myGameplayData.active || activeGameplay) {
                    toolbar.title = "Gameplay"
                    Log.e("lala", "otvara se my gameplay")
                    val myGameplayFragment = MyGameplayActivity.newInstance()
                    openFragment(myGameplayFragment)
                    addGeofence()

                } else {
                    toolbar.title = "Games"
                    GamesData.fetchAllGamesData(object : AllGamesDataCallback {
                        override fun onAllGamesDataCallback(AllGamesData: java.util.ArrayList<GameCard>) {
                            Log.e("lala", "otvara se my gameplay")
                            val gamesFragment = AllGamesActivity.newInstance()
                            openFragment(gamesFragment)
                        }
                    })
                }
            }
        })
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.e("permission fully, " , "granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            FINE_LOCATION_ACCESS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Log.e("FINE_LOCATION", " not granted")
                }
                return
            }
            BACKGROUND_LOCATION_ACCESS_REQUEST_CODE-> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Log.e("BACKGROUND_LOCATION", " not granted")
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun addGeofence() {

        var gameLat = GameplayData.myActiveGameplay.lat.toDouble()
        var gameLon = GameplayData.myActiveGameplay.lon.toDouble()
        var myLat = currentLat?.toDouble()
        var myLon = currentLon?.toDouble()

        var distanceHalf = (distance(myLat!!, myLon!!, gameLat, gameLon) * 1000).toFloat()

        //Log.e("lat", myLat.toString())
        //Log.e("lon", myLon.toString())
        //Log.e("gamelat", gameLat.toString())
        //Log.e("gamelon", gameLon.toString())
        //Log.e("distancehalf", distanceHalf.toString())

        var geofence = geofenceHelper.getGeofence(GEOFENCE_ID, myLat, myLon, distanceHalf, Geofence.GEOFENCE_TRANSITION_DWELL);
        var geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        var pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnCompleteListener {
                    addOnSuccessListener {
                        Toast.makeText(this@MainActivity, "Geofence active!",
                            Toast.LENGTH_SHORT)
                            .show()
                        Log.e("Add Geofence", geofence.requestId)
                    }
                    addOnFailureListener {
                        Toast.makeText(this@MainActivity, "Geofence not active!",
                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w("Add Geofence", it.message)
                        }
                    }
                }
            }
        }


    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_myGames -> {
                toolbar.title = "My Games"
                GamesData.myGamesData.clear()
                GamesData.fetchMyGamesData(object : MyGamesDataCallback {
                    override fun onMyGamesDataCallback(myGroupsData: ArrayList<GameCard>) {
                        val myGamesFragment = MyGamesActivity.newInstance()
                        openFragment(myGamesFragment)
                    }
                })
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_games -> {
                GameplayData.fetchMyActiveGameplayData(object : MyActiveGameplayDataCallback {
                    override fun onMyActiveGameplayDataCallback(myGameplayData: GameplayCard) {
                        Log.e("fetching gameplay", myGameplayData.toString())
                        if (myGameplayData.active || activeGameplay) {
                            toolbar.title = "Gameplay"
                                Log.e("lala", "otvara se my gameplay")
                                val myGameplayFragment = MyGameplayActivity.newInstance()
                                openFragment(myGameplayFragment)
                                addGeofence()
                        } else {
                            toolbar.title = "Games"
                            GamesData.fetchAllGamesData(object : AllGamesDataCallback {
                                override fun onAllGamesDataCallback(AllGamesData: java.util.ArrayList<GameCard>) {
                                    Log.e("lala", "otvara se my gameplay")
                                    val gamesFragment = AllGamesActivity.newInstance()
                                    openFragment(gamesFragment)
                                }
                            })
                        }
                    }
                })

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_leaderboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myProfile-> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            this.let {
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

        fusedLocationClient = this.let { LocationServices.getFusedLocationProviderClient(it) }!!
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
        if (this.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } == PackageManager.PERMISSION_GRANTED &&
            this.let {
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
        this.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                42
            )
        }
    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
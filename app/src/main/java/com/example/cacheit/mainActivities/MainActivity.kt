package com.example.cacheit.mainActivities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cacheit.*
import com.example.cacheit.R
import com.example.cacheit.gameplayActivities.GameplayCard
import com.example.cacheit.gameplayActivities.GameplayData
import com.example.cacheit.gameplayActivities.MyGameplayActivity
import com.example.cacheit.gamesActivities.AllGamesActivity
import com.example.cacheit.gamesActivities.GameCard
import com.example.cacheit.gamesActivities.GamesData
import com.example.cacheit.leaderboardActivities.LeaderboardActivity
import com.example.cacheit.leaderboardActivities.PlayerCard
import com.example.cacheit.leaderboardActivities.PlayerData
import com.example.cacheit.myGamesActivities.MyGamesActivity
import com.example.cacheit.shared.LocationActivites.Companion.distance
import com.example.cacheit.userProfileActivities.UserProfileActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var geofencingClient: GeofencingClient

    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire
    private var mainHandler: Handler? =  null
    // current user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private var FINE_LOCATION_ACCESS_REQUEST_CODE: Int = 10001;
    private var BACKGROUND_LOCATION_ACCESS_REQUEST_CODE: Int = 10002;
    private var notifyCounter: Int = 0
    private var keepGoingFlag: Boolean = false

    companion object {
        var activeGameplay = false;
        var mainContext = this;
        internal const val ACTION_GEOFENCE_EVENT = "MainActivity.cacheit.action.ACTION_GEOFENCE_EVENT"
        var currentLat: Double? = null;
        var currentLon: Double? = null;
    }

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        createNotificationChannel()

        foregroundAndBackgroundLocationPermissionApproved()

        // initialize FusedLocationProviderClient
        fusedLocationClient = this.let { LocationServices.getFusedLocationProviderClient(it) }!!
        getLastLocation()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.navigation_games;
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

        Log.e("my active gameplay",  GameplayData.myActiveGameplay.toString())
        var gameLat = GameplayData.myActiveGameplay.lat.toDouble()
        var gameLon = GameplayData.myActiveGameplay.lon.toDouble()
        var myLat = currentLat?.toDouble()
        var myLon = currentLon?.toDouble()

        var distance = ((distance(myLat!!, myLon!!, gameLat, gameLon)).toDouble() / 2) //km

        val userId = Firebase.auth!!.currentUser!!.uid
        //setting up GeoFire
        geoFire = GeoFire(FirebaseDatabase.getInstance().getReference("MyLocation/${userId}"))
        geoFire.setLocation("you", GeoLocation(myLat, myLon))
//        if(geoQuery != null) {
//            geoQuery!!.removeAllListeners()
//        }

        if(geoQuery == null) geoQuery = geoFire.queryAtLocation(GeoLocation(gameLat, gameLon), distance) //km

        Log.e("active gameplay name", GameplayData.myActiveGameplay.name)
        Log.e("lat", myLat.toString())
        Log.e("lon", myLon.toString())
        Log.e("gamelat", gameLat.toString())
        Log.e("gamelon", gameLon.toString())
        Log.e("distancehalf", distance.toString())

        geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
                Log.e("geo wuery ready ", " sam")
            }
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.e("ulazim", " sam")
                sendNotification("Entered the game finish zone", "Awesome! You are on the right path!")
                keepGoingFlag = true

            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Log.e("krecem se ", " sam")
                if (keepGoingFlag) sendNotification("Moving in the game finish zone", "Keep going! The game finish is near.")
                keepGoingFlag = false
            }

            override fun onKeyExited(key: String?) {
                Log.e("izlazim ", " sam")
                sendNotification("Exited the game finish zone", "Go back! You are on the wrong path!")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.e("error ", " sam")
                sendNotification("ERROR", "I'm sorry... We can't track your progress, try again later.")
            }
        })
    }

    private fun sendNotification(title: String, body: String) {
        var builder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.drawable.chest)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notifyCounter, builder.build())
            notifyCounter++
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "1"
            val descriptionText = "RMA notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
                GameplayData.myActiveGameplay = GameplayCard()
                GameplayData.fetchMyActiveGameplayData(object : MyActiveGameplayDataCallback {
                    override fun onMyActiveGameplayDataCallback(myGameplayData: GameplayCard) {
                        if (myGameplayData.active || activeGameplay) {
                                val myGameplayFragment = MyGameplayActivity.newInstance()
                                openFragment(myGameplayFragment)
                                addGeofence()
                                mainHandler = Handler(Looper.getMainLooper())
                                mainHandler!!.post(object : Runnable {
                                    override fun run() {
                                        getLastLocation()
                                        geoFire.setLocation("you", GeoLocation(currentLat!!, currentLon!!))
                                        mainHandler!!.postDelayed(this, 5000) //every 5 seconds
                                    }
                                })
                        } else {
                            if (!activeGameplay && mainHandler != null)  mainHandler!!.removeCallbacksAndMessages(null);
                            GamesData.fetchAllGamesData(object : AllGamesDataCallback {
                                override fun onAllGamesDataCallback(AllGamesData: java.util.ArrayList<GameCard>) {
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
                PlayerData.bestMakersData.clear()
                PlayerData.bestPlayersData.clear()
                PlayerData.fetchBestPlayersData(object : MyPlayersDataCallback {
                    override fun onMyPlayersDataCallback(MyPlayersDataCallback: java.util.ArrayList<PlayerCard>) {
                        PlayerData.fetchBestMakersData(object : MyMakersDataCallback {
                            override fun onMyMakersDataCallback(MyMakersDataCallback: java.util.ArrayList<PlayerCard>) {
                                val leaderboardFragment = LeaderboardActivity.newInstance()
                                openFragment(leaderboardFragment)
                            }

                        })
                    }
                })
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myProfile-> {
                val userProfileFragment = UserProfileActivity.newInstance()
                openFragment(userProfileFragment)
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
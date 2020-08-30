package com.example.cacheit.gameplayActivities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Rating
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.cacheit.R
import com.example.cacheit.mainActivities.MainActivity
import com.example.cacheit.mainActivities.MainActivity.Companion.currentLat
import com.example.cacheit.mainActivities.MainActivity.Companion.currentLon
import com.example.cacheit.shared.GeofenceHelper
import com.example.cacheit.shared.LocationActivites.Companion.distance
import com.google.android.gms.location.LocationServices
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_qr_code_scanner.*
import java.util.*
import kotlin.concurrent.schedule

class QRcodeScannerActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var btnCloseScanner: ImageButton
    private var ratingScore: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        if(ContextCompat.checkSelfPermission(this@QRcodeScannerActivity,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            askForCameraPermission()
        } else {
            setupControls()
        }
        val btnCloseScanner: ImageButton = findViewById(R.id.btn_close_scanner)

        btnCloseScanner!!.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
        }
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(this@QRcodeScannerActivity).build()
        cameraSource = CameraSource.Builder(this@QRcodeScannerActivity, detector)
            .setAutoFocusEnabled(true)
            .build()
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(this@QRcodeScannerActivity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {

        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
            try {
                cameraSource.start(surfaceHolder)
            } catch (exception: Exception) {
                Toast.makeText(applicationContext, "Something went wrong.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)

                this@QRcodeScannerActivity.runOnUiThread(java.lang.Runnable {
                    cameraSource.stop()
                    detector.release()
                    showEnding(code.displayValue) //TO-DO: add check if user within 100m radius of gameLat, gameLon
                });
                //code.displayValue //gameId!!!!!!!!!!!!!!!!!!
            } else {
                //nothing detected
            }
        }

    }

    private fun showEnding(gameId : String) {
      val dialog = Dialog(this)
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setCancelable(false)
      dialog.setContentView(R.layout.layout_game_solved)

      val btnSave = dialog.findViewById(R.id.btn_save) as Button
      val name = dialog.findViewById(R.id.tv_game_details_name) as TextView
      val totalTime = dialog.findViewById(R.id.tv_times_played) as TextView
      val pointsEarned = dialog.findViewById(R.id.tv_times_solved) as TextView
      val cbReportGame = dialog.findViewById(R.id.cb_report_game) as CheckBox
      val ratingBar = dialog.findViewById(R.id.rb_game_rating) as RatingBar
      var gameDeleted = false

      ratingBar.onRatingBarChangeListener = this

      name.text = GameplayData.myActiveGameplay.name
      totalTime.text = GameplayData.myActiveGameplay.totalTime
      pointsEarned.text = calculatePoints()
      dialog.show()

      btnSave!!.setOnClickListener { v ->

          //update rating and report game if checked
          var mDatabase: FirebaseDatabase? = null
          mDatabase = FirebaseDatabase.getInstance()
          val ref = mDatabase!!.getReference("/Games/" + GameplayData.myActiveGameplay.gameId)

          var timesReported = 0
          ref.addListenerForSingleValueEvent(object : ValueEventListener {
              override fun onCancelled(p0: DatabaseError) {
                  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
              }

              override fun onDataChange(p0: DataSnapshot) {
                  if (cbReportGame.isChecked) {
                      timesReported = p0.child("timesReported").value.toString().toInt()
                      if (timesReported == 5) {
                          ref.child("deleted").setValue(true) //if game reported 5 times, deleted
                          gameDeleted = true
                      }
                      ref.child("timesReported").setValue((timesReported + 1))
                  }
                  var ratingCount = p0.child("ratingCount").value.toString().toInt() + 1
                  var makerPoints = p0.child("gameMakerPoints").value.toString().toInt()

                  if (cbReportGame.isChecked) {
                      ref.child("gameMakerPoints").setValue(makerPoints - makerPoints/10)
                  } else {
                      ref.child("gameMakerPoints").setValue(makerPoints + (calculatePoints().toFloat().toInt() * ratingScore) / 10)
                  }


                  var rating = p0.child("rating").value.toString().toFloat().toInt() + ratingBar.rating
                  var finishedCount = p0.child("timesFinished").value.toString().toInt() + 1
                  ref.child("ratingCount").setValue(ratingCount)
                  ref.child("rating").setValue(rating/ratingCount)
                  ref.child("timesFinished").setValue(finishedCount)

              }
          })

          //

          // increment player score
          mDatabase = FirebaseDatabase.getInstance()
          val refPlayer = mDatabase!!.getReference("/Users/" + GameplayData.myActiveGameplay.playerId)

          refPlayer.addListenerForSingleValueEvent(object : ValueEventListener {
              override fun onCancelled(p0: DatabaseError) {
                  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
              }

              override fun onDataChange(p0: DataSnapshot) {
                  val playerScore = p0.child("playerScore").value.toString().toInt() + calculatePoints().toFloat().toInt()
                  refPlayer.child("playerScore").setValue(playerScore)
              }
          })
          //

          // increment gameMaker score
          val refOwner = mDatabase!!.getReference("/Users/" + GameplayData.myActiveGameplay.gameMakerId)

          refOwner.addListenerForSingleValueEvent(object : ValueEventListener {
              override fun onCancelled(p0: DatabaseError) {
                  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
              }

              override fun onDataChange(p0: DataSnapshot) {
                  if (!p0.hasChild(("makerScore"))) return
                  val gameMakerPoints = p0.child("playerScore").value.toString().toFloat().toInt()

                  if (cbReportGame.isChecked) {
                      refOwner.child("makerScore").setValue(gameMakerPoints - gameMakerPoints/10)
                  } else {
                      refOwner.child("makerScore").setValue(gameMakerPoints + (calculatePoints().toFloat().toInt() * ratingScore) / 10)
                  }
              }
          })
          //

          // deactivate gameplay
          val refGameplay = mDatabase!!.getReference("/Gameplays/" + GameplayData.myActiveGameplay.gameplayId)

          refGameplay.addListenerForSingleValueEvent(object : ValueEventListener {
              override fun onCancelled(p0: DatabaseError) {
                  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
              }

              override fun onDataChange(p0: DataSnapshot) {
                  val points = p0.child("points").value.toString().toFloat().toInt()
                  if (cbReportGame.isChecked) {
                      refGameplay.child("points").setValue(points - points/10)
                  } else {
                      refGameplay.child("points").setValue(points + (calculatePoints().toFloat().toInt() * ratingScore) / 10)
                  }
                  refGameplay.child("active").setValue(false)
                  refGameplay.child("completed").setValue(true)
                  MainActivity.activeGameplay = false
                  GameplayData.myActiveGameplay.active = false
                  refGameplay.child("totalTime").setValue(GameplayData.myActiveGameplay.totalTime)
                  dialog.dismiss()
                  MainActivity.activeGameplay = false
                  GameplayData.myActiveGameplay.active = false
                  exitScanner()
              }
          })
          dialog.dismiss()
          MainActivity.activeGameplay = false
          GameplayData.myActiveGameplay.active = false
      }
    }

    override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
        ratingScore = p1
    }

    private fun calculatePoints(): String {
        var difficultyPoints = 0;
        var timePredicted = 0 // in seconds
        if (GameplayData.myActiveGameplay.difficulty == "Easy") {
            difficultyPoints += 5
            timePredicted = 60 * 60 * 2
        } else if (GameplayData.myActiveGameplay.difficulty == "Medium") {
            difficultyPoints += 10
            timePredicted = 60 * 60 * 3
        } else {
            difficultyPoints += 15
            timePredicted = 60 * 60 * 4
        }
        val distanceTravelledPoints = distance(currentLat!!, currentLon!!, GameplayData.myActiveGameplay.lat.toDouble(), GameplayData.myActiveGameplay.lon.toDouble()) * 100
        val timePoints = timePredicted / GameplayData.myActiveGameplay.totalTime.toFloat().toInt()

        val totalPoints = if ((distanceTravelledPoints + timePoints + difficultyPoints)  > 200) {
            200.0
        } else {
            (distanceTravelledPoints + timePoints + difficultyPoints)
        }
        return totalPoints.toString()
    }

    private fun exitScanner() {
        this@QRcodeScannerActivity.finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        this.startActivity(intent)
    }
}
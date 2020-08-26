package com.example.cacheit.gameplayActivities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.example.cacheit.mainActivities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class MyGameplayActivity : Fragment() {

    private var root: View? = null
    private var tvGameName: TextView? = null
    private var tvGameHint: TextView? = null
    private var btnStop: ImageButton? = null
    private var btnCamera: ImageButton? = null
    private var meter: Chronometer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_my_gameplay, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root
    }

    private fun initialise() {
        Log.e("lala", "entered mygamepaly activity")
        meter = root?.findViewById<Chronometer>(R.id.c_meter)
        Log.e("meter ret", meter?.text.toString())
        if (meter != null) {
            if (meter!!.text.toString() != "00:00") return
        }
        var dateDifferenceMiliseconds = dateDifference()
        meter?.onChronometerTickListener = OnChronometerTickListener { chronometer ->
            val time = SystemClock.elapsedRealtime() - chronometer.base + dateDifferenceMiliseconds
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val t =
                (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
            chronometer.text = t
        }
        meter?.start()

        tvGameName = root?.findViewById<TextView>(R.id.tv_gameName) as TextView
        tvGameHint = root?.findViewById<TextView>(R.id.tv_gameHint) as TextView

        btnStop = root?.findViewById<ImageButton>(R.id.btn_stop) as ImageButton
        btnCamera = root?.findViewById<ImageButton>(R.id.btn_camera) as ImageButton

        tvGameName?.text = GameplayData.myActiveGameplay.name
        tvGameHint?.text = GameplayData.myActiveGameplay.hint

        btnStop!!.setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(activity)
            dialogBuilder.setMessage("Are you sure you want to cancel the gameplay? (your progress will be saved)")
                // if the dialog is cancelable
                .setCancelable(true)
                .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                    saveGameProgress()
                    exitGameplay()
                }

            val alert = dialogBuilder.create()
            alert.show()
        }

        btnCamera!!.setOnClickListener{
            var mDatabase: FirebaseDatabase? = null
            val ref = mDatabase?.getReference("/Gameplays/" + GameplayData.myActiveGameplay.gameplayId)
            var gamePlayedSeconds = (SystemClock.elapsedRealtime() - meter!!.base)/1000 + GameplayData.myActiveGameplay.totalTime.toFloat();
            ref?.child("totalTime")?.setValue(gamePlayedSeconds.toString())
            GameplayData.myActiveGameplay.totalTime = gamePlayedSeconds.toString()

            val intent = Intent(activity, QRcodeScannerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity?.startActivity(intent)
        }
    }

    private fun saveGameProgress() {
        var mDatabase: FirebaseDatabase? = null
        mDatabase = FirebaseDatabase.getInstance()
        val ref = mDatabase.getReference("/Gameplays/" + GameplayData.myActiveGameplay.gameplayId)
        ref.child("active").setValue(false)
        GameplayData.myActiveGameplay.active = false

        var gamePlayedSeconds = (SystemClock.elapsedRealtime() - meter!!.base)/1000 + GameplayData.myActiveGameplay.totalTime.toFloat();
        ref.child("totalTime").setValue(gamePlayedSeconds.toString())
    }

    private fun exitGameplay() {
        MainActivity.activeGameplay = false
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity?.startActivity(intent)
    }

    private fun dateDifference(): Long {
        val simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return (Date(simpleDateFormat.format(Date())).time - Date(GameplayData.myActiveGameplay.dateStarted).time + GameplayData.myActiveGameplay.totalTime.toFloat().toLong()*1000)
    }

    companion object {
        fun newInstance(): MyGameplayActivity = MyGameplayActivity()
    }
}
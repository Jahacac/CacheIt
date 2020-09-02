package com.example.cacheit.gameplayActivities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.Chronometer.OnChronometerTickListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.example.cacheit.gameplayActivities.GameplayData.Companion.myActiveGameplay
import com.example.cacheit.mainActivities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*


class MyGameplayActivity : Fragment() {

    private var root: View? = null
    private var tvGameName: TextView? = null
    private var tvGameHint: TextView? = null
    private var btnStop: ImageButton? = null
    private var btnCamera: ImageButton? = null
    private var img: CircleImageView? = null
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
        img = root?.findViewById<CircleImageView>(R.id.logo) as CircleImageView

        Glide.with(requireActivity())
            //.applyDefaultRequestOptions(requestOptions)
            .load(GameplayData.myActiveGameplay.gameImg)
            .into(img!!)

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

        if ((SystemClock.elapsedRealtime() - meter!!.base)/1000 + GameplayData.myActiveGameplay.totalTime.toFloat() >= 86400.0f) {
            var btn_report = root?.findViewById<ImageButton>(R.id.btn_report) as ImageButton
            btn_report.visibility = VISIBLE
            btn_report!!.setOnClickListener{
                openReportDialog()
            }
        }
    }

    private fun openReportDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_report_game)

        val btnSave = dialog.findViewById(R.id.btn_save_report) as Button
        val btnExit = dialog.findViewById(R.id.btn_close_report) as ImageButton

        btnExit!!.setOnClickListener{
            dialog.dismiss();
        }

        btnSave!!.setOnClickListener { v ->
            var mDatabase: FirebaseDatabase? = null
            mDatabase = FirebaseDatabase.getInstance()
            val ref = mDatabase!!.getReference("/Games/" + myActiveGameplay.gameId)

            val refGameplay = mDatabase!!.getReference("/Gameplays/" + myActiveGameplay.gameplayId)
            var timesReported = 0
            refGameplay.child("reported").setValue(true)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    timesReported = p0.child("timesReported").value.toString().toInt()
                    if (timesReported == 5) {
                        ref.child("deleted").setValue(true)
                    }
                    ref.child("timesReported").setValue((timesReported + 1))

                    refGameplay.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            Log.e("ref", ref.toString())
                            Log.e("refgamepaly", refGameplay.toString())
                            refGameplay.child("completed").setValue(true)
                            refGameplay.child("active").setValue(false)

                        }
                    })
                    dialog.dismiss();
                    Toast.makeText(activity, "Game reported!", Toast.LENGTH_SHORT).show()
                    exitGameplay()
                }
            })
        }
        dialog.show()
    }
    private fun saveGameProgress() {
        var mDatabase: FirebaseDatabase? = null
        mDatabase = FirebaseDatabase.getInstance()
        val ref = mDatabase.getReference("/Gameplays/" + GameplayData.myActiveGameplay.gameplayId)
        ref.child("active").setValue(false)
        GameplayData.myActiveGameplay.active = false

        var gamePlayedSeconds = (SystemClock.elapsedRealtime() - meter!!.base)/1000 + GameplayData.myActiveGameplay.totalTime.toFloat();
        ref.child("totalTime").setValue(gamePlayedSeconds.toString())
        exitGameplay()
    }

    private fun exitGameplay() {
        MainActivity.activeGameplay = false
        myActiveGameplay = GameplayCard()
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        getFragmentManager()?.beginTransaction()?.remove(this)?.commitAllowingStateLoss();
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
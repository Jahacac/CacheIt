package com.example.cacheit.gameplayActivities

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import androidx.fragment.app.Fragment
import com.example.cacheit.R
import java.text.SimpleDateFormat
import java.util.*


class MyGameplayActivity : Fragment() {

    private var root: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_my_gameplay, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root
    }

    private fun initialise() {
        Log.e("lala", "entered mygamepaly activity")
        val meter = root?.findViewById<Chronometer>(R.id.c_meter)
        Log.e("meter ret", meter?.text.toString())
        if (meter != null) {
            if (meter.text.toString() != "00:00") return
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

    }

    private fun dateDifference(): Long {
        val simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return (Date(simpleDateFormat.format(Date())).time - Date(GameplayData.myActiveGameplay.dateStarted).time)
    }

    companion object {
        fun newInstance(): MyGameplayActivity = MyGameplayActivity()
    }
}
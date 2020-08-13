package com.example.cacheit.createGameActivities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cacheit.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateGameActivity : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.activity_create_game, container, false)

    companion object {
        fun newInstance(): CreateGameActivity = CreateGameActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialise()
    }

    private fun initialise() {
    }
}
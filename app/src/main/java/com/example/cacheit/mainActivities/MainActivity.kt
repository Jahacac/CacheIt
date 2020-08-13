package com.example.cacheit.mainActivities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cacheit.R
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_createGame -> {
                toolbar.title = "Create Game"
                val createGameFragment = CreateGameActivity.newInstance()
                openFragment(createGameFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_games -> {
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
}
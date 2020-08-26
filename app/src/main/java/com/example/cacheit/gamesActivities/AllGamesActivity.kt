package com.example.cacheit.gamesActivities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity
import com.example.cacheit.gameplayActivities.MyGameplayActivity
import com.example.cacheit.gamesActivities.GamesData
import kotlinx.android.synthetic.main.activity_my_games.*
import kotlinx.coroutines.withContext

class AllGamesActivity : Fragment() {

    private lateinit var allGamesRecyclerAdapter: AllGamesRecyclerAdapter
    private var root: View? = null
    private var rvAllGames: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_all_games, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root

    }

    private fun initialise() {
        initRecyclerView()
        addDataSet()
    }


    private fun addDataSet() {
        Log.e(tag, "Fetched user's games: " + GamesData.myGamesData)
        allGamesRecyclerAdapter.submitList(GamesData.allGamesData)
    }

    private fun initRecyclerView() {
        rvAllGames = root?.findViewById<RecyclerView>(R.id.rv_all_games)
        rvAllGames?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            allGamesRecyclerAdapter = AllGamesRecyclerAdapter()
            adapter = allGamesRecyclerAdapter
        }
    }

    companion object {
        fun newInstance(): AllGamesActivity = AllGamesActivity()
    }

}
package com.example.cacheit.leaderboardActivities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cacheit.R

class LeaderboardActivity : Fragment() {

    private lateinit var playerLeaderboardRecyclerAdapter: PlayersLeaderboardRecyclerAdapter
    private lateinit var makersLeaderboardRecyclerAdapter: MakersLeaderboardRecyclerAdapter

    private var root: View? = null
    private var rvPlayers: RecyclerView? = null
    private var rvMakers: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_leaderboard, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root

    }

    private fun initialise() {
        Log.e("best players", PlayerData.bestPlayersData.toString())
        Log.e("best makers", PlayerData.bestMakersData.toString())
        initRecyclerViewPlayers()
        initRecyclerViewMakers()
        addDataSetPlayers()
        addDataSetMakers()
    }

    private fun addDataSetMakers() {
        Log.e(tag, "Fetched user's games: " + PlayerData.bestMakersData)
        makersLeaderboardRecyclerAdapter.submitList(PlayerData.bestMakersData)
    }

    private fun initRecyclerViewMakers() {
        rvMakers = root?.findViewById<RecyclerView>(R.id.rv_game_makers)
        rvMakers?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            makersLeaderboardRecyclerAdapter = MakersLeaderboardRecyclerAdapter()
            adapter = makersLeaderboardRecyclerAdapter
        }
    }


    private fun addDataSetPlayers() {
        Log.e(tag, "Fetched user's games: " + PlayerData.bestPlayersData)
        playerLeaderboardRecyclerAdapter.submitList(PlayerData.bestPlayersData)
    }

    private fun initRecyclerViewPlayers() {
        rvPlayers = root?.findViewById<RecyclerView>(R.id.rv_players)
        rvPlayers?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            playerLeaderboardRecyclerAdapter = PlayersLeaderboardRecyclerAdapter()
            adapter = playerLeaderboardRecyclerAdapter
        }
    }

    companion object {
        fun newInstance(): LeaderboardActivity = LeaderboardActivity()
    }

}
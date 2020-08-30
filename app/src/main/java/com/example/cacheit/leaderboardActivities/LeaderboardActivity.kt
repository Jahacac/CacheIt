package com.example.cacheit.leaderboardActivities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cacheit.R
import com.example.cacheit.createGameActivities.CreateGameActivity

class LeaderboardActivity : Fragment() {

    private lateinit var playerLeaderboardRecyclerAdapter: PlayersLeaderboardRecyclerAdapter
    private lateinit var makersLeaderboardRecyclerAdapter: MakersLeaderboardRecyclerAdapter

    private var root: View? = null
    private var rvPlayers: RecyclerView? = null
    private var rvMakers: RecyclerView? = null
    private var btnPlayers: Button? = null
    private var btnGameMakers: Button? = null
    //#131468

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_leaderboard, container, false)
        super.onCreate(savedInstanceState)

        btnPlayers = root?.findViewById<Button>(R.id.btn_players)
        btnGameMakers = root?.findViewById<Button>(R.id.btn_game_makers)

        btnPlayers!!.setOnClickListener{
            if (rvPlayers?.visibility != VISIBLE) {
                rvMakers?.visibility = GONE
                rvPlayers?.visibility = VISIBLE
                btnPlayers!!.setTextColor(Color.parseColor("#FFFFFF"))
                btnPlayers!!.setBackgroundResource(R.drawable.select_btn);
                btnGameMakers!!.setTextColor(Color.parseColor("#131468"))
                btnGameMakers!!.setBackgroundResource(R.drawable.unselect_btn);
            }
        }

        btnGameMakers!!.setOnClickListener{
            if (rvMakers?.visibility != VISIBLE) {
                rvPlayers?.visibility = GONE
                rvMakers?.visibility = VISIBLE
                btnGameMakers!!.setTextColor(Color.parseColor("#FFFFFF"))
                btnGameMakers!!.setBackgroundResource(R.drawable.select_btn);
                btnPlayers!!.setTextColor(Color.parseColor("#131468"))
                btnPlayers!!.setBackgroundResource(R.drawable.unselect_btn);
            }
        }

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
package com.example.cacheit.myGamesActivities

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
import com.example.cacheit.gamesActivities.GamesData
import kotlinx.android.synthetic.main.activity_my_games.*
import kotlinx.coroutines.withContext

class MyGamesActivity : Fragment() {

    private lateinit var myGamesRecyclerAdapter: MyGamesRecyclerAdapter
    private var root: View? = null
    private var btnCreateNewGame: Button? = null
    private var rvMyGames: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_my_games, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root
    }

    private fun initialise() {
        initRecyclerView()
        addDataSet()

        btnCreateNewGame = root?.findViewById<Button>(R.id.btn_create_new) as Button

        btnCreateNewGame!!.setOnClickListener{
            val createGameFragment = CreateGameActivity.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.my_games,createGameFragment).commit()
        }

    }


    private fun addDataSet() {
        Log.e(tag, "Fetched user's games: " + GamesData.myGamesData)
        myGamesRecyclerAdapter.submitList(GamesData.myGamesData)
    }

    private fun initRecyclerView() {
        rvMyGames = root?.findViewById<RecyclerView>(R.id.rv_my_games)
        rvMyGames?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            myGamesRecyclerAdapter = MyGamesRecyclerAdapter()
            adapter = myGamesRecyclerAdapter
        }
    }

    companion object {
        fun newInstance(): MyGamesActivity = MyGamesActivity()
    }

}
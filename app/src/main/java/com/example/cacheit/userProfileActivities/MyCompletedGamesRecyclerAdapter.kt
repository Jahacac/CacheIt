package com.example.cacheit.userProfileActivities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cacheit.Firebase
import com.example.cacheit.MyActiveGameplayDataCallback
import com.example.cacheit.MySavedGameplayDataCallback
import com.example.cacheit.R
import com.example.cacheit.gameplayActivities.GameplayCard
import com.example.cacheit.gameplayActivities.GameplayData
import com.example.cacheit.gamesActivities.GameCard
import com.example.cacheit.mainActivities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user_profile.view.*
import kotlinx.android.synthetic.main.layout_completed_game_card_item.view.*
import kotlinx.android.synthetic.main.layout_playable_game_card_item.view.*
import kotlinx.android.synthetic.main.layout_playable_game_card_item.view.img_game_card
import kotlinx.android.synthetic.main.layout_playable_game_card_item.view.tv_game_card_name
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyCompletedGamesRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<GameplayCard> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyCompletedGamesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_completed_game_card_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is MyCompletedGamesViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(CompletedGameplayList: List<GameplayCard>) {
        items = CompletedGameplayList
        notifyDataSetChanged()
    }

    class MyCompletedGamesViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val gameImage = itemView.img_game_card
        private val gameName = itemView.tv_game_card_name
        private val pointsEarned = itemView.tv_points_earned
        private val difficulty = itemView.tv_difficulty

        fun bind(GameplayCard: GameplayCard) {
            gameName.text = GameplayCard.name
            pointsEarned.text = GameplayCard.points
            difficulty.text = GameplayCard.difficulty

            Glide.with(itemView.context)
                .load(GameplayCard.gameImg)
                .into(gameImage)
        }
    }

}


package com.example.cacheit.leaderboardActivities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cacheit.R
import kotlinx.android.synthetic.main.layout_player_card_item.view.*

class MakersLeaderboardRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<PlayerCard> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyGamesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_player_card_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is MyGamesViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(GroupCardList: List<PlayerCard>) {
        items = GroupCardList
        notifyDataSetChanged()
    }

    class MyGamesViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val playerImage = itemView.img_game_card
        private val playerUsername = itemView.tv_game_card_name
        private val playerPoints = itemView.tv_status
        private val playerRank = itemView.tv_rank

        fun bind(PlayerCard: PlayerCard) {
            playerUsername.text = PlayerCard.username
            playerPoints.text = PlayerCard.makerScore.toString()
            playerRank.text = PlayerCard.order.toString() + "."

            Glide.with(itemView.context)
                //.applyDefaultRequestOptions(requestOptions)
                .load(PlayerCard.profileImage)
                .into(playerImage)
        }
    }

}
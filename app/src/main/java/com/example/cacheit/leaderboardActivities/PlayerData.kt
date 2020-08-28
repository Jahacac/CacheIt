package com.example.cacheit.leaderboardActivities

import android.util.Log
import com.example.cacheit.MyMakersDataCallback
import com.example.cacheit.MyPlayersDataCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.*
import kotlin.collections.ArrayList


class PlayerData {
    companion object {
        var bestPlayersData = ArrayList<PlayerCard>()
        var bestMakersData = ArrayList<PlayerCard>()

        fun fetchBestPlayersData(myPlayersDataCallback: MyPlayersDataCallback) {
            Log.e("fetch my games data", "function called")

            bestPlayersData = ArrayList()
            FirebaseDatabase.getInstance().getReference("/Users/").orderByChild("playerScore")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("myPlayersDataCallback", "Failed to read value. " + p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0.children
                        var counter = 0
                        children.forEach {
                            bestPlayersData.add(
                                PlayerCard(
                                    it.child("username").value.toString(),
                                    it.child("profilePhotoUrl").value.toString(),
                                    it.child("playerScore").value.toString().toInt(),
                                    it.child("makerScore").value.toString().toInt(),
                                    p0.children.count() - counter
                                )
                            )
                            counter += 1
                        }
                        Log.i("returning value: ", bestPlayersData.toString())
                        bestPlayersData.reverse();
                        myPlayersDataCallback.onMyPlayersDataCallback(bestPlayersData)
                    }
                })
        }

        fun fetchBestMakersData(myMakersDataCallback: MyMakersDataCallback) {
            Log.e("fetch my games data", "function called")

            bestMakersData = ArrayList()
            FirebaseDatabase.getInstance().getReference("/Users/").orderByChild("makerScore")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("myPlayersDataCallback", "Failed to read value. " + p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0.children
                        var counter = 0
                        children.forEach {
                            bestMakersData.add(
                                PlayerCard(
                                    it.child("username").value.toString(),
                                    it.child("profilePhotoUrl").value.toString(),
                                    it.child("playerScore").value.toString().toInt(),
                                    it.child("makerScore").value.toString().toInt(),
                                    p0.children.count() - counter
                                )
                            )
                            counter += 1
                        }
                        bestMakersData.reverse();
                        Log.i("returning value: ", bestMakersData.toString())
                        myMakersDataCallback.onMyMakersDataCallback(bestMakersData)
                    }
                })
        }
    }

}
package com.example.cacheit.gameplayActivities

import android.util.Log
import com.example.cacheit.Firebase
import com.example.cacheit.MyActiveGameplayDataCallback
import com.example.cacheit.MyCompletedGameplayDataCallback
import com.example.cacheit.MySavedGameplayDataCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class GameplayData {

    companion object {

        var myActiveGameplay = GameplayCard()
        var mySavedGameplay = ArrayList<GameplayCard>()
        var myCompletedGameplays = ArrayList<GameplayCard>()

        fun fetchMyActiveGameplayData(myActiveGameplayDataCallback: MyActiveGameplayDataCallback) {
            Log.e("fetch my games data", "function called")
            Firebase.databaseGameplays!!
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("fetchMyGamesData", "Failed to read value. " + p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0.children
                        val userId = Firebase.auth!!.currentUser!!.uid
                        children.forEach {
                            if (it.child("playerId").value.toString() == userId && it.child("active").value.toString().toBoolean()) {
                                Log.e("if ownerId: ", "match found!")
                                myActiveGameplay =
                                    GameplayCard(
                                        it.child("gameplayId").value.toString(),
                                        it.child("gameId").value.toString(),
                                        it.child("playerId").value.toString(),
                                        it.child("totalTime").value.toString(),
                                        it.child("completed").value.toString().toBoolean(),
                                        it.child("active").value.toString().toBoolean(),
                                        it.child("points").value.toString(),
                                        it.child("dateStarted").value.toString(),
                                        it.child("lat").value.toString(),
                                        it.child("lon").value.toString(),
                                        it.child("name").value.toString(),
                                        it.child("string").value.toString(),
                                        it.child("gameMakerId").value.toString(),
                                        it.child("difficulty").value.toString(),
                                        it.child("initialDistance").value.toString(),
                                        it.child("gameImg").value.toString()
                                    )
                            }
                        }
                        Log.i("returning value: ", myActiveGameplay.toString())
                        myActiveGameplayDataCallback.onMyActiveGameplayDataCallback(myActiveGameplay)
                    }
                })
        }

        fun fetchMySavedGameplayData(mySavedGameplayDataCallback: MySavedGameplayDataCallback) {
            Log.e("fetch my games data", "function called")
            Firebase.databaseGameplays!!
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("fetchMyGamesData", "Failed to read value. " + p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0.children
                        val userId = Firebase.auth!!.currentUser!!.uid
                        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                        val currentDate = sdf.format(Date())

                        children.forEach {
                            if (it.child("playerId").value.toString() == userId) {
                                Log.e("lala ", "igram staru igru")
                                Log.e("saved gameplay: ", "match found!")
                                it.ref.child("active").setValue(true)
                                it.ref.child("dateStarted").setValue(currentDate)
                                mySavedGameplay.add(
                                    GameplayCard(
                                        it.child("gameplayId").value.toString(),
                                        it.child("gameId").value.toString(),
                                        it.child("playerId").value.toString(),
                                        it.child("totalTime").value.toString(),
                                        it.child("completed").value.toString().toBoolean(),
                                        it.child("active").value.toString().toBoolean(),
                                        it.child("points").value.toString(),
                                        it.child("dateStarted").value.toString(),
                                        it.child("lat").value.toString(),
                                        it.child("lon").value.toString(),
                                        it.child("name").value.toString(),
                                        it.child("string").value.toString(),
                                        it.child("gameMakerId").value.toString(),
                                        it.child("difficulty").value.toString(),
                                        it.child("initialDistance").value.toString(),
                                        it.child("gameImg").value.toString()
                                    )
                                )
                            }
                        }
                        Log.i("returning value: ", mySavedGameplay.toString())
                        mySavedGameplayDataCallback.onMySavedGameplayDataCallback(mySavedGameplay)
                    }
                })
        }

        fun fetchMyCompletedGameplayData(myCompletedGameplayDataCallback: MyCompletedGameplayDataCallback) {
            myCompletedGameplays.clear()
            Log.e("fetch my games data", "function called")
            Firebase.databaseGameplays!!
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("fetchMyGamesData", "Failed to read value. " + p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0.children
                        val userId = Firebase.auth!!.currentUser!!.uid
                        children.forEach {
                            if (it.child("playerId").value.toString() == userId && it.child("completed").value.toString().toBoolean()) {
                                Log.e("if ownerId: ", "match found!")
                                myCompletedGameplays.add(
                                    GameplayCard(
                                        it.child("gameplayId").value.toString(),
                                        it.child("gameId").value.toString(),
                                        it.child("playerId").value.toString(),
                                        it.child("totalTime").value.toString(),
                                        it.child("completed").value.toString().toBoolean(),
                                        it.child("active").value.toString().toBoolean(),
                                        it.child("points").value.toString(),
                                        it.child("dateStarted").value.toString(),
                                        it.child("lat").value.toString(),
                                        it.child("lon").value.toString(),
                                        it.child("name").value.toString(),
                                        it.child("string").value.toString(),
                                        it.child("gameMakerId").value.toString(),
                                        it.child("difficulty").value.toString(),
                                        it.child("initialDistance").value.toString()
                                    )
                                )
                            }
                        }
                        Log.i("returning value: ", myCompletedGameplays.toString())
                        myCompletedGameplayDataCallback.onMyCompletedGameplayDataCallback(myCompletedGameplays)
                    }
                })
        }
    }
}
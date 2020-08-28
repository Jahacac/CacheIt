package com.example.cacheit

import java.util.ArrayList
import com.example.cacheit.gamesActivities.GameCard
import com.example.cacheit.gameplayActivities.GameplayCard
import com.example.cacheit.leaderboardActivities.PlayerCard


interface MyGamesDataCallback {
    fun onMyGamesDataCallback(myGamesData: ArrayList<GameCard>)
}

interface AllGamesDataCallback {
    fun onAllGamesDataCallback(AllGamesData: ArrayList<GameCard>)
}

interface MyActiveGameplayDataCallback {
    fun onMyActiveGameplayDataCallback(MyGameplayData: GameplayCard)
}

interface MySavedGameplayDataCallback {
    fun onMySavedGameplayDataCallback(MySavedGameplayData: ArrayList<GameplayCard>)
}

interface MyPlayersDataCallback {
    fun onMyPlayersDataCallback(MyPlayersDataCallback: ArrayList<PlayerCard>)
}

interface MyMakersDataCallback {
    fun onMyMakersDataCallback(MyMakersDataCallback: ArrayList<PlayerCard>)
}
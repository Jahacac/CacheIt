package com.example.cacheit

import java.util.ArrayList
import com.example.cacheit.gamesActivities.GameCard
import com.example.cacheit.gameplayActivities.GameplayCard


interface MyGamesDataCallback {
    fun onMyGamesDataCallback(myGamesData: ArrayList<GameCard>)
}

interface AllGamesDataCallback {
    fun onAllGamesDataCallback(AllGamesData: ArrayList<GameCard>)
}

interface MyActiveGameplayDataCallback {
    fun onMyActiveGameplayDataCallback(MyGameplayData: GameplayCard)
}

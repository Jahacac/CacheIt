package com.example.cacheit.gameplayActivities

import java.time.LocalDateTime

data class GameplayCard (
    var gameplayId: String = "",    //gameplay Id
    var gameId: String = "",        //game Id
    var playerId: String = "",      //player Id
    var totalTime: String = "",        //time played
    var completed: Boolean = false,    //completed flag (true - finished, false - unfinished)
    var active: Boolean = false,       //active tag (true - currently played, false - not currently played)
    var points: String = "",            //points earned
    var dateStarted: String = "",
    var lat: String = "",
    var lon: String = "",
    var name: String = "",
    var hint: String = "",
    var gameMakerId: String = "",
    var difficulty: String = "",
    var initialDistance: String = "",
    var gameImg: String = "",
    var reported: Boolean = false
)
package com.example.cacheit.gamesActivities

data class GameCard (
    var id: String,                 // game id
    var name: String,               // game name
    var hint: String,               // game hint
    var ownerId: String,            // game owner id (from Users table)
    var rating: String,             // game total rating
    var ratingCount: String,        // game rating count (for calculating game total rating)
    var locationHint: Boolean,      // game location flag hint (true/false)
    var gameImg: String,            // game image uri
    var difficulty: String,         // game difficulty (easy, medium, hard)
    var active: Boolean,            // game activity status (true, false)
    var deleted: Boolean,           // game deleted (true, false) -> cannot be recovered!
    var timesReported: String,
    var lat: String,       // 3 reports result in game being deactivated
    var lon: String,
    var timesFinished: String,
    var timesClosed: String,
    var gameMakerPoints: String
    )

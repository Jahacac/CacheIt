package com.example.cacheit.leaderboardActivities

data class PlayerCard(
    var username: String,
    var profileImage: String,
    var playerScore: Int,
    var makerScore: Int,
    var order: Int
)
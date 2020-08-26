package com.example.cacheit.userProfileActivities

data class UserCard(
    var id: String,
    var fullName: String,
    var username: String,
    var profilePhotoUrl: String,
    var email: String,
    var playerScore: String,
    var gameMakerScore: String
)
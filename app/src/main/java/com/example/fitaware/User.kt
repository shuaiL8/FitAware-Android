package com.example.fitaware

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String? = "",
    var email: String? = "",
    var password: String? = "",
    var team: String? = "",
    var captain: String? = "",
    var currentSteps: String? = "",
    var dailyGoal: String? = ""

) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "password" to password,
            "team" to team,
            "captain" to captain,
            "currentSteps" to currentSteps,
            "dailyGoal" to dailyGoal

        )
    }
}
package com.smittd.tutorapp.model

data class Meeting(
    val id: String = "",
    val date: String = "",      // np. "2025-01-25"
    val time: String = "",      // np. "18:00"
    val subject: String = "",
    val teacherName: String = "",
    val studentName: String = "",
    val onlineLink: String = ""
)
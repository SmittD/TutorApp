package com.smittd.tutorapp.model

/**
 * Model danych użytkownika przechowywany w Realtime Database pod węzłem:
 * users/{uid}
 */
data class UserData(
    val email: String = "",
    val role: String = ""
    // Dodaj więcej pól, jeśli potrzebujesz (np. displayName, phone, itp.)
)
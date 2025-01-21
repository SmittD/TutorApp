package com.smittd.tutorapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().reference
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user == null) {
            // Nie zalogowany
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Mamy usera, pobieramy role
            val uid = user.uid
            dbRef.child("users").child(uid).child("role").get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.value?.toString() ?: "student"
                    Log.d("SPLASH_SCREEN", "role = $role (from RTDB)")
                    Toast.makeText(context, "Rola: $role", Toast.LENGTH_SHORT).show()

                    if (role == "admin") {
                        navController.navigate("adminPanel") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SPLASH_SCREEN", "Błąd pobierania roli: ${e.message}", e)
                    Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Ostatecznie, jak nie uda się pobrać roli, można iść do login
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
        }
    }

    // Ekran ładowania / splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            UserGreeting()
            Text("Wczytywanie...")
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}
@Composable
fun UserGreeting() {
    val usergreetings = FirebaseAuth.getInstance().currentUser
    val emailgreetings = usergreetings?.email ?: "Nieznajomy"
    Text("Witaj, $emailgreetings")
}
package com.smittd.tutorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.smittd.tutorapp.model.UserData

@Composable
fun AdminPanelScreen(navController: NavController) {
    val rtdbRef = FirebaseDatabase.getInstance().reference
    var userList by remember { mutableStateOf<List<Pair<String, UserData>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    // Odczyt listy userów
    LaunchedEffect(Unit) {
        rtdbRef.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = mutableListOf<Pair<String, UserData>>()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val userData = child.getValue(UserData::class.java)
                        if (userData != null) {
                            tempList.add(uid to userData)
                        }
                    }
                    userList = tempList
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = "Błąd pobierania: ${error.message}"
                }
            })
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        UserGreeting()  // (Jeśli masz taki Composable do pokazywania np. "Witaj, admin@example.com")

        Text(text = "Panel administratora", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do tworzenia nowych kont
        Button(
            onClick = {
                // Nawigacja do ekranu tworzenia użytkownika
                navController.navigate("createUserScreen")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dodaj użytkownika")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do tworzenia spotkań
        Button(
            onClick = {
                // Nawigacja do ekranu tworzenia spotkania
                navController.navigate("createMeetingScreen")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Utwórz spotkanie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do pisania globalnej wiadomości
        Button(
            onClick = {
                navController.navigate("createGlobalMessageScreen")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Napisz globalną wiadomość")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk wylogowania
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Wyloguj się")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Lista użytkowników
        userList.forEach { (uid, userData) ->
            UserItemRow(uid, userData)
        }
    }
}

@Composable
fun UserItemRow(uid: String, userData: UserData) {
    val rtdbRef = FirebaseDatabase.getInstance().reference
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("student", "teacher", "admin")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Email: ${userData.email}")
            Text(text = "Rola: ${userData.role}")
        }

        Box {
            Button(onClick = { expanded = true }) {
                Text("Zmień rolę")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                roles.forEach { newRole ->
                    DropdownMenuItem(
                        text = { Text(newRole) },
                        onClick = {
                            expanded = false
                            rtdbRef.child("users").child(uid).child("role").setValue(newRole)
                        }
                    )
                }
            }
        }

        Button(onClick = {
            rtdbRef.child("users").child(uid).removeValue()
        }) {
            Text("Usuń")
        }
    }
}
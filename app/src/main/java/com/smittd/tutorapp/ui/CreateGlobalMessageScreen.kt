package com.smittd.tutorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun CreateGlobalMessageScreen(navController: NavController) {
    val dbRef = FirebaseDatabase.getInstance().reference

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Napisz globalną wiadomość", style = MaterialTheme.typography.titleLarge)

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Tytuł") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Treść") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Zapis do /messages
                val newKey = dbRef.child("messages").push().key
                if (newKey != null) {
                    val msgMap = mapOf(
                        "id" to newKey,
                        "title" to title,
                        "content" to content
                    )
                    dbRef.child("messages").child(newKey).setValue(msgMap)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            errorMessage = "Błąd zapisu wiadomości: ${it.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz")
        }
    }
}
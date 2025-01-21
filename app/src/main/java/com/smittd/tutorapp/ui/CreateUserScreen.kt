package com.smittd.tutorapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.smittd.tutorapp.model.UserData

/**
 * Ekran tworzenia nowego konta (np. tylko dla admina).
 * Tworzy konto w Firebase Auth oraz zapisuje dane w Realtime Database:
 * users/{uid} -> { email, role }
 */
@Composable
fun CreateUserScreen() {
    val auth = FirebaseAuth.getInstance()
    val rtdbRef = FirebaseDatabase.getInstance().reference
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") } // Domyślna rola
    var isLoading by remember { mutableStateOf(false) }

    // Dostępne role
    val roles = listOf("student", "teacher", "admin")
    var dropdownExpanded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        UserGreeting()
        Text("Tworzenie nowego użytkownika", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown do wyboru roli
        Box {
            OutlinedTextField(
                value = role,
                onValueChange = {},
                label = { Text("Rola") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { dropdownExpanded = true },
                readOnly = true
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                roles.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r) },
                        onClick = {
                            role = r
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val adminEmail = "admin@admin.admin"
        val adminPass = "admin123"

        Button(
            onClick = {
                isLoading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                val userData = UserData(
                                    email = user.email ?: "",
                                    role = role
                                )
                                // Zapis do RTDB: /users/{uid}
                                rtdbRef.child("users").child(user.uid)
                                    .setValue(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Konto utworzone: ${user.email}", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                        auth.signInWithEmailAndPassword(adminEmail, adminPass)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Błąd zapisu: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Błąd tworzenia konta: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Stwórz konto")
        }
    }
}
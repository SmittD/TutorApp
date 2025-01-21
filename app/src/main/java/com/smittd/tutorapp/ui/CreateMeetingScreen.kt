package com.smittd.tutorapp.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.smittd.tutorapp.model.Meeting
import com.smittd.tutorapp.model.UserData
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun CreateMeetingScreen(navController: NavController) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val context = LocalContext.current

    // Stan do formularza
    var subject by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") } // np. "2025-02-10"
    var time by remember { mutableStateOf("") } // np. "10:30"
    var onlineLink by remember { mutableStateOf("") }

    // Stan do dropdownu teacher/student
    var selectedTeacher by remember { mutableStateOf<UserData?>(null) }
    var selectedStudent by remember { mutableStateOf<UserData?>(null) }

    // Pobierz listę teacherów i studentów, by admin mógł wybrać
    var teachers by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var students by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dbRef.child("users").get().addOnSuccessListener { snapshot ->
            val allTeachers = mutableListOf<UserData>()
            val allStudents = mutableListOf<UserData>()
            for (child in snapshot.children) {
                val user = child.getValue(UserData::class.java)
                if (user != null) {
                    if (user.role == "teacher") {
                        allTeachers.add(user)
                    } else if (user.role == "student") {
                        allStudents.add(user)
                    }
                }
            }
            teachers = allTeachers
            students = allStudents
        }.addOnFailureListener {
            errorMessage = "Błąd pobierania userów: ${it.message}"
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tworzenie spotkania", style = MaterialTheme.typography.titleLarge)

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        // Dropdown teacher
        TeacherDropdown(
            teachers = teachers,
            selectedTeacher = selectedTeacher,
            onTeacherSelected = { selectedTeacher = it }
        )

        Spacer(Modifier.height(8.dp))

        // Dropdown student
        StudentDropdown(
            students = students,
            selectedStudent = selectedStudent,
            onStudentSelected = { selectedStudent = it }
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Przedmiot") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Data (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Godzina (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = onlineLink,
            onValueChange = { onlineLink = it },
            label = { Text("Link do spotkania") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedTeacher == null || selectedStudent == null || subject.isBlank() || date.isBlank() || time.isBlank()) {
                    Toast.makeText(context, "Proszę wypełnić wszystkie pola.", Toast.LENGTH_SHORT).show()
                } else {
                    // Zapis do RTDB
                    val newKey = dbRef.child("meetings").push().key
                    if (newKey != null) {
                        val meeting = mapOf<String, Any>(
                            "id" to newKey,
                            "date" to date,
                            "time" to time,
                            "subject" to subject,
                            "teacherName" to selectedTeacher!!.email, // Można dać imię
                            "studentName" to selectedStudent!!.email,
                            "onlineLink" to onlineLink
                        )
                        dbRef.child("meetings").child(newKey).setValue(meeting)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Spotkanie zapisane!", Toast.LENGTH_SHORT).show()
                                // Wraca do adminPanel
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                errorMessage = "Błąd zapisu spotkania: ${it.message}"
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz spotkanie")
        }
    }
}

@Composable
fun TeacherDropdown(
    teachers: List<UserData>,
    selectedTeacher: UserData?,
    onTeacherSelected: (UserData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        OutlinedTextField(
            value = selectedTeacher?.email ?: "Wybierz nauczyciela",
            onValueChange = {},
            readOnly = true, // Ustawienie readOnly zamiast enabled
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nauczyciel") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            teachers.forEach { teacher ->
                DropdownMenuItem(
                    text = { Text(teacher.email) },
                    onClick = {
                        onTeacherSelected(teacher)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StudentDropdown(
    students: List<UserData>,
    selectedStudent: UserData?,
    onStudentSelected: (UserData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        OutlinedTextField(
            value = selectedStudent?.email ?: "Wybierz studenta",
            onValueChange = {},
            readOnly = true, // Ustawienie readOnly zamiast enabled
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Student") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            students.forEach { student ->
                DropdownMenuItem(
                    text = { Text(student.email) },
                    onClick = {
                        onStudentSelected(student)
                        expanded = false
                    }
                )
            }
        }
    }
}
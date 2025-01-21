package com.smittd.tutorapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.smittd.tutorapp.model.Meeting
import com.smittd.tutorapp.model.Material
import com.smittd.tutorapp.model.GlobalMessage

// Drobna enum do rozróżnienia zakładek
enum class HomeTab { HOME, MATERIALS, MESSAGES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().reference

    // Stan na rolę usera ("student", "teacher", "")
    var userRole by remember { mutableStateOf("") }
    var roleError by remember { mutableStateOf("") }

    // Gdy user tutaj trafi, z SplashScreen wiemy, że to nie admin.
    // Ale możemy jeszcze raz pobrać w RTDB rolę -> "student"/"teacher".
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            dbRef.child("users").child(uid).child("role").get()
                .addOnSuccessListener {
                    val role = it.value?.toString() ?: "student"
                    userRole = role
                }
                .addOnFailureListener { e ->
                    roleError = "Błąd pobierania roli: ${e.message}"
                }
        } else {
            // Nie powinno się zdarzyć, ale na wszelki wypadek:
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Stan wybranej zakładki w bottom nav
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ekran główny ($userRole, ${auth.currentUser?.email})")
                },
                actions = {Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Wyloguj się")
                }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        // Zawartość zależna od wybranej zakładki
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (roleError.isNotEmpty()) {
                Text(text = "Błąd: $roleError", color = MaterialTheme.colorScheme.error)
            } else {
                when (selectedTab) {
                    HomeTab.HOME -> HomeTabContent()
                    HomeTab.MATERIALS -> MaterialsTab(userRole = userRole)
                    HomeTab.MESSAGES -> MessagesTab()
                }
            }
        }
    }
    }

/**
 * Dolny pasek nawigacji (3 ikony/zakładki).
 */
@Composable
fun BottomNavBar(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == HomeTab.HOME,
            onClick = { onSelectTab(HomeTab.HOME) },
            label = { Text("Home") },
            icon = { /* Ikonka, jeśli chcesz */ }
        )
        NavigationBarItem(
            selected = selectedTab == HomeTab.MATERIALS,
            onClick = { onSelectTab(HomeTab.MATERIALS) },
            label = { Text("Materiały") },
            icon = { /* Ikonka */ }
        )
        NavigationBarItem(
            selected = selectedTab == HomeTab.MESSAGES,
            onClick = { onSelectTab(HomeTab.MESSAGES) },
            label = { Text("Wiadomości") },
            icon = { /* Ikonka */ }
        )
    }
}

/**
 * Zakładka HOME -> kalendarz + lista spotkań (meetings z RTDB).
 */
@Composable
fun HomeTabContent() {
    // 1. Placeholder kalendarza
    CalendarPlaceholder()

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Pobranie listy spotkań
    val dbRef = FirebaseDatabase.getInstance().reference
    var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }
    var loadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dbRef.child("meetings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Meeting>()
                for (child in snapshot.children) {
                    val m = child.getValue(Meeting::class.java)
                    if (m != null) tempList.add(m)
                }
                meetings = tempList
            }

            override fun onCancelled(error: DatabaseError) {
                loadError = "Błąd: ${error.message}"
            }
        })
    }

    Text("Twoje spotkania:", style = MaterialTheme.typography.titleMedium)
    if (loadError.isNotEmpty()) {
        Text(loadError, color = MaterialTheme.colorScheme.error)
    } else {
        if (meetings.isEmpty()) {
            Text("Brak zaplanowanych spotkań.")
        } else {
            for (meet in meetings) {
                MeetingItem(meet)
                Divider()
            }
        }
    }
}

@Composable
fun CalendarPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Kalendarz - bieżący miesiąc")
            Text("Tutaj zostaną wyświetlione faktyczna siatka dni itp.")
        }
    }
}

/**
 * Jeden element Meeting
 */
@Composable
fun MeetingItem(m: Meeting) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Data: ${m.date}, Godz: ${m.time}")
        Text("Przedmiot: ${m.subject}")
        Text("Nauczyciel: ${m.teacherName}")
        Text("Uczeń: ${m.studentName}")
        if (m.onlineLink.isNotBlank()) {
            Text("Link: ${m.onlineLink}", color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Zakładka "Materiały" -> różne UI dla teacher vs student.
 */
@Composable
fun MaterialsTab(userRole: String) {
    val dbRef = FirebaseDatabase.getInstance().reference
    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dbRef.child("materials").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Material>()
                for (child in snapshot.children) {
                    val mat = child.getValue(Material::class.java)
                    if (mat != null) {
                        tempList.add(mat)
                    }
                }
                materials = tempList
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Błąd: ${error.message}"
            }
        })
    }

    Text("Materiały", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    if (errorMessage.isNotEmpty()) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
    }

    // Teacher -> może dodać
    if (userRole == "teacher") {
        Button(onClick = {
            // TODO: Kod do dodawania materiału (otwórz Dialog, zapisz w RTDB)
        }) {
            Text("Dodaj nowy materiał")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (materials.isEmpty()) {
        Text("Brak materiałów.")
    } else {
        for (m in materials) {
            MaterialItem(m)
            Divider()
        }
    }
}

@Composable
fun MaterialItem(m: Material) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Tytuł: ${m.title}")
        Text("Link: ${m.url}", color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Zakładka "Wiadomości" (globalne ogłoszenia).
 */
@Composable
fun MessagesTab() {
    val dbRef = FirebaseDatabase.getInstance().reference
    var messages by remember { mutableStateOf<List<GlobalMessage>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dbRef.child("messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<GlobalMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(GlobalMessage::class.java)
                    if (msg != null) {
                        tempList.add(msg)
                    }
                }
                messages = tempList
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Błąd: ${error.message}"
            }
        })
    }

    Text("Wiadomości ogólne", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    if (errorMessage.isNotEmpty()) {
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
    }

    if (messages.isEmpty()) {
        Text("Brak wiadomości.")
    } else {
        for (m in messages) {
            MessageItem(m)
            Divider()
        }
    }
}

@Composable
fun MessageItem(msg: GlobalMessage) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(msg.title, style = MaterialTheme.typography.titleSmall)
        Text(msg.content)
    }
}
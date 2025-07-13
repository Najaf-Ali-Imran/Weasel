package com.example.weasel.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppMessage(
    val text: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)

class MessageViewModel : ViewModel() {
    private val database = Firebase.database.getReference("announcement")

    private val _message = MutableStateFlow<AppMessage?>(null)
    val message = _message.asStateFlow()

    private val messageListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _message.value = snapshot.getValue(AppMessage::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    init {
        database.addValueEventListener(messageListener)
    }

    override fun onCleared() {
        database.removeEventListener(messageListener)
        super.onCleared()
    }
}
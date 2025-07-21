// In: app/src/main/java/com/example/weasel/data/Playlist.kt
package com.example.weasel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val thumbnailUrl: String? = null
)
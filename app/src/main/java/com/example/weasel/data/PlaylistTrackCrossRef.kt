package com.example.weasel.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["playlistId", "trackId"],
    indices = [Index(value = ["trackId"])]
)
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: String
)
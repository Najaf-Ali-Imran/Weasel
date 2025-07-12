package com.example.weasel.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    @ColumnInfo(defaultValue = "0")
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)
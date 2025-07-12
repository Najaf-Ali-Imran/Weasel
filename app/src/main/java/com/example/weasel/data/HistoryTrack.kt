package com.example.weasel.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playedAt"]), Index(value = ["trackId"])]
)
data class HistoryTrack(
    @PrimaryKey val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)
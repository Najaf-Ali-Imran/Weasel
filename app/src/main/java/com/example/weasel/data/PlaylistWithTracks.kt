package com.example.weasel.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id", // The primary key column in the Playlist table
        entityColumn = "id",  // The primary key column in the Track table

        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = "playlistId", // Column in the junction table for the Playlist
            entityColumn = "trackId"      // Column in the junction table for the Track
        )
    )
    val tracks: List<Track>
)
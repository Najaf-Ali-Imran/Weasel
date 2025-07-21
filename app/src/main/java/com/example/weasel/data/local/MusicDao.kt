package com.example.weasel.data.local

import androidx.room.*
import com.example.weasel.data.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToHistory(historyTrack: HistoryTrack)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTracksToPlaylist(crossRefs: List<PlaylistTrackCrossRef>)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT *, history.playedAt FROM tracks INNER JOIN history ON tracks.id = history.trackId ORDER BY history.playedAt DESC LIMIT 50")
    fun getHistoryWithTimestamp(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist): Long

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): Playlist?
    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(tracks: List<Track>)
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks>

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM PlaylistTrackCrossRef WHERE playlistId = :playlistId AND trackId IN (:trackIds)")
    suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<String>)

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracks(): Flow<List<Track>>

    @Query("UPDATE tracks SET isDownloaded = 1, localPath = :filePath WHERE id = :trackId")
    suspend fun updateTrackAsDownloaded(trackId: String, filePath: String)
}


data class HistoryEntry(
    @Embedded val track: Track,
    val playedAt: Long,
    @ColumnInfo(name = "trackId") val trackId: String?,
)
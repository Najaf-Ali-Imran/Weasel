package com.example.weasel.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.room.Transaction
import com.example.weasel.data.Playlist
import com.example.weasel.data.PlaylistTrackCrossRef
import com.example.weasel.data.PlaylistWithTracks
import com.example.weasel.data.Track
import com.example.weasel.data.HistoryTrack
import com.example.weasel.data.local.HistoryEntry
import com.example.weasel.data.local.MusicDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
class LocalMusicRepository(private val musicDao: MusicDao, private val context: Context) {

    fun getHistory(): Flow<List<HistoryEntry>> = musicDao.getHistoryWithTimestamp()

    fun getPlaylists(): Flow<List<Playlist>> = musicDao.getPlaylists()

    suspend fun createPlaylist(name: String) {
        musicDao.createPlaylist(Playlist(name = name))
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        musicDao.updatePlaylist(playlist)
    }

    suspend fun addToHistory(track: Track) {
        musicDao.insertTrack(track)
        musicDao.addTrackToHistory(HistoryTrack(trackId = track.id))
    }


    @Transaction
    suspend fun insertAndAddTracksToPlaylist(playlistId: Long, tracks: List<Track>) {
        musicDao.insertTracks(tracks)

        val crossRefs = tracks.map { track ->
            PlaylistTrackCrossRef(playlistId = playlistId, trackId = track.id)
        }
        musicDao.addTracksToPlaylist(crossRefs)
    }

    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<String>) {
        val crossRefs = trackIds.map { trackId ->
            PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId)
        }
        musicDao.addTracksToPlaylist(crossRefs)
    }

    suspend fun insertTrack(track: Track) {
        musicDao.insertTrack(track)
    }

//    @Transaction
//    suspend fun addTracksToPlaylist(playlistId: Long, tracks: List<String>) {
//        musicDao.insertTracks(tracks)
//
//        val crossRefs = tracks.map { track ->
//            PlaylistTrackCrossRef(playlistId = playlistId, trackId = track.id)
//        }
//        musicDao.addTracksToPlaylist(crossRefs)
//    }
    fun getDownloadedTracks(): Flow<List<Track>> = musicDao.getDownloadedTracks()

    suspend fun setTrackAsDownloaded(trackId: String, filePath: String) {
        musicDao.updateTrackAsDownloaded(trackId, filePath)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<String>) {
        musicDao.removeTracksFromPlaylist(playlistId, trackIds)
    }

    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks> {
        return musicDao.getPlaylistWithTracks(playlistId)
    }

    suspend fun getPlaylistByName(name: String): Playlist? {
        return musicDao.getPlaylistByName(name)
    }

    suspend fun getLocalAudioFiles(): List<Track> = withContext(Dispatchers.IO) {
        val localTracks = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val artworkUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                localTracks.add(
                    Track(
                        id = contentUri.toString(),
                        title = title,
                        artist = artist ?: "Unknown Artist",
                        thumbnailUrl = artworkUri.toString()
                    )
                )
            }
        }
        return@withContext localTracks
    }
}
package com.example.weasel.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.weasel.data.local.AppDatabase
import com.example.weasel.repository.MusicRepository
import com.example.weasel.repository.NewPipeMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val musicDao = AppDatabase.getDatabase(applicationContext).musicDao()
    private val musicRepository: MusicRepository = NewPipeMusicRepository()
    private val httpClient = OkHttpClient()

    override suspend fun doWork(): Result {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: return Result.failure()
        val trackTitle = inputData.getString(KEY_TRACK_TITLE) ?: "track"
        val trackArtist = inputData.getString(KEY_TRACK_ARTIST) ?: "unknown"

        val initialProgress = workDataOf(
            KEY_PROGRESS to 0,
            KEY_TRACK_TITLE to trackTitle,
            KEY_TRACK_ARTIST to trackArtist
        )
        setProgress(initialProgress)

        return withContext(Dispatchers.IO) {
            try {
                val streamResult = musicRepository.getAudioStreamUrl(trackId)
                streamResult.fold(
                    onSuccess = { url ->
                        val request = Request.Builder().url(url).build()
                        val response: Response = httpClient.newCall(request).execute()
                        val responseBody = response.body ?: return@withContext Result.failure()

                        val resolver = applicationContext.contentResolver

                        val audioCollection =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                            } else {
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }

                        val sanitizedTitle = trackTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        val sanitizedArtist = trackArtist.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        val fileName = "$sanitizedTitle - $sanitizedArtist.m4a"

                        val newSongDetails = ContentValues().apply {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + File.separator + "Weasel")
                                put(MediaStore.Audio.Media.IS_PENDING, 1)
                            } else {
                                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                                val weaselDir = File(musicDir, "Weasel")
                                if (!weaselDir.exists()) {
                                    weaselDir.mkdirs()
                                }
                                val file = File(weaselDir, fileName)
                                put(MediaStore.Audio.Media.DATA, file.absolutePath)
                            }
                        }

                        val songUri = resolver.insert(audioCollection, newSongDetails)
                            ?: return@withContext Result.failure()

                        resolver.openOutputStream(songUri).use { outputStream ->
                            if (outputStream == null) return@withContext Result.failure()

                            val totalBytes = responseBody.contentLength()
                            responseBody.byteStream().use { inputStream ->
                                val buffer = ByteArray(8 * 1024) // 8KB buffer
                                var bytesCopied: Long = 0
                                var bytes = inputStream.read(buffer)
                                while (bytes >= 0) {
                                    outputStream.write(buffer, 0, bytes)
                                    bytesCopied += bytes
                                    bytes = inputStream.read(buffer)
                                    if (totalBytes > 0) {
                                        val progress = (bytesCopied * 100 / totalBytes).toInt()
                                        val progressData = workDataOf(
                                            KEY_PROGRESS to progress,
                                            KEY_TRACK_TITLE to trackTitle,
                                            KEY_TRACK_ARTIST to trackArtist
                                        )
                                        setProgress(progressData)
                                    }
                                }
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            newSongDetails.clear()
                            newSongDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
                            resolver.update(songUri, newSongDetails, null, null)
                        }

                        musicDao.updateTrackAsDownloaded(trackId, songUri.toString())

                        Result.success()
                    },
                    onFailure = { Result.failure() }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_TRACK_ID = "TRACK_ID"
        const val KEY_TRACK_TITLE = "TRACK_TITLE"
        const val KEY_TRACK_ARTIST = "TRACK_ARTIST"
        const val KEY_PROGRESS = "PROGRESS"
        const val DOWNLOAD_TAG = "download_work"
    }
}
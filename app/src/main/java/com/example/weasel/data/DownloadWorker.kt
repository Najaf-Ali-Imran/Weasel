package com.example.weasel.data

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

                        val downloadsDir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                            "Weasel"
                        )
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()

                        val sanitizedTitle = trackTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        val sanitizedArtist = trackArtist.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                        val fileName = "$sanitizedTitle - $sanitizedArtist.m4a"
                        val file = File(downloadsDir, fileName)

                        val responseBody = response.body
                        if (responseBody != null) {
                            val totalBytes = responseBody.contentLength()
                            responseBody.byteStream().use { input ->
                                BufferedOutputStream(FileOutputStream(file)).use { output ->
                                    // Can increase buffer to increase downloading speed
                                    val buffer = ByteArray(32 * 1024) // 32KB buffer
                                    var bytesCopied: Long = 0
                                    var bytes = input.read(buffer)
                                    while (bytes >= 0) {
                                        output.write(buffer, 0, bytes)
                                        bytesCopied += bytes
                                        bytes = input.read(buffer)
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
                        } else {
                            response.close()
                            return@withContext Result.failure()
                        }

                        response.close()
                        val filePath = file.absolutePath
                        MediaScannerConnection.scanFile(applicationContext, arrayOf(filePath), null, null)
                        musicDao.updateTrackAsDownloaded(trackId, filePath)

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
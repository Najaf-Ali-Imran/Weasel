package com.example.weasel.data

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object DownloaderImpl : Downloader() {

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            chain.proceed(request)
        }
        .build()

    override fun execute(request: Request): Response {
        val httpRequest = okhttp3.Request.Builder()
            .url(request.url())
            .apply {
                // Add headers
                request.headers()?.forEach { (key, values) ->
                    values.forEach { value ->
                        addHeader(key, value)
                    }
                }

                // Handle different HTTP methods
                when (request.httpMethod()) {
                    "GET" -> get()
                    "POST" -> {
                        val body = request.dataToSend()?.toRequestBody()
                        post(body ?: "".toRequestBody())
                    }
                    "HEAD" -> head()
                }
            }
            .build()

        return try {
            val response = client.newCall(httpRequest).execute()
            Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body?.string(),
                response.request.url.toString()
            )
        } catch (e: Exception) {
            throw ReCaptchaException("Request failed", request.url())
        }
    }
}
package com.example.weasel.repository

import com.example.weasel.data.Track

interface MusicRepository {
    suspend fun searchMusic(query: String): Result<List<Track>>
    suspend fun getAudioStreamUrl(trackUrl: String): Result<String>
}
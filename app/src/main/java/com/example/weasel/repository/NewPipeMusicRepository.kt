package com.example.weasel.repository

import com.example.weasel.data.DownloaderImpl
import com.example.weasel.data.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class NewPipeMusicRepository : MusicRepository {

    init {
        NewPipe.init(DownloaderImpl)
    }

    override suspend fun searchMusic(query: String): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(100) // Rate limiting

                val searchExtractor = ServiceList.YouTube.getSearchExtractor(query, emptyList(), null)
                searchExtractor.fetchPage()

                val initialPage = searchExtractor.initialPage
                val items = initialPage.items

                val tracks = items
                    .filterIsInstance<StreamInfoItem>()
                    .mapNotNull { streamItem ->
                        try {
                            Track(
                                id = streamItem.url,
                                title = streamItem.name,
                                artist = streamItem.uploaderName ?: "Unknown Artist",
                                thumbnailUrl = streamItem.thumbnails.firstOrNull()?.url ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(Exception("Search failed: ${e.message}", e))
            }
        }
    }

    /**
     * Get trending music tracks by searching for popular music content
     * Since NewPipe doesn't have a dedicated trending music kiosk, we use search with music-specific queries
     */
    suspend fun getTrendingTracks(
        countryCode: String = "PK",
        maxResults: Int = 50
    ): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.delay(100) // Rate limiting

            // Use search queries that typically return trending music
            val musicSearchQueries = listOf(
                "trending music 2024",
                "popular songs",
                "latest music",
                "top hits",
                "music charts"
            )

            val allTracks = mutableListOf<Track>()

            for (query in musicSearchQueries) {
                if (allTracks.size >= maxResults) break

                try {
                    val service = ServiceList.YouTube
                    val searchExtractor = service.getSearchExtractor(query, emptyList(), null)

                    // Set localization for country-specific results
                    searchExtractor.forceLocalization(Localization("en", "GB"))
                    searchExtractor.forceContentCountry(ContentCountry(countryCode))

                    searchExtractor.fetchPage()
                    val items = searchExtractor.initialPage.items

                    val tracks = items
                        .filterIsInstance<StreamInfoItem>()
                        .filter { isMusicContent(it) }
                        .take((maxResults - allTracks.size).coerceAtLeast(0))
                        .mapNotNull { it.toTrack() }
                        .filter { track ->
                            // Avoid duplicates
                            allTracks.none { existingTrack ->
                                existingTrack.id == track.id ||
                                        (existingTrack.title.equals(track.title, ignoreCase = true) &&
                                                existingTrack.artist.equals(track.artist, ignoreCase = true))
                            }
                        }

                    allTracks.addAll(tracks)

                    kotlinx.coroutines.delay(200)
                } catch (e: Exception) {
                    continue
                }
            }

            Result.success(allTracks.take(maxResults))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch trending tracks: ${e.message}", e))
        }
    }

    // Extension function to map StreamInfoItem to Track
    private fun StreamInfoItem.toTrack() = try {
        Track(
            id = this.url,
            title = cleanTitle(this.name),
            artist = this.uploaderName ?: "Unknown Artist",
            thumbnailUrl = getBestThumbnail(this.thumbnails)
        )
    } catch (e: Exception) {
        null
    }

    /**
     * Alternative method to get trending music using YouTube's browse functionality
     * This attempts to use the music browse endpoint if available
     */
    suspend fun getTrendingMusicAlternative(
        countryCode: String = "US",
        maxResults: Int = 50
    ): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.delay(100) // Rate limiting

            val service = ServiceList.YouTube
            val country = ContentCountry(countryCode)
            val locale = Localization("en", "GB")

            val kioskList = service.kioskList
            val trendingKiosk = kioskList.getDefaultKioskExtractor()

            trendingKiosk.forceLocalization(locale)
            trendingKiosk.forceContentCountry(country)

            trendingKiosk.fetchPage()
            val items = trendingKiosk.initialPage.items

            val tracks = items
                .filterIsInstance<StreamInfoItem>()
                .filter { isStrictMusicContent(it) }
                .take(maxResults)
                .mapNotNull { it.toTrack() }

            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch trending music: ${e.message}", e))
        }
    }
    suspend fun getTrendingByCountries(
        countryCodes: List<String> = listOf("US", "GB"),
        maxResultsPerCountry: Int = 20
    ): Result<Map<String, List<Track>>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableMapOf<String, List<Track>>()

                for (countryCode in countryCodes) {
                    try {
                        val result = getTrendingTracks(countryCode, maxResultsPerCountry)
                        result.onSuccess { tracks ->
                            results[countryCode] = tracks
                        }
                        kotlinx.coroutines.delay(200)
                    } catch (e: Exception) {
                        continue
                    }
                }

                Result.success(results)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch trending for multiple countries: ${e.message}", e))
            }
        }
    }

    // Additional method for pagination - returns SearchResult with next page info
    suspend fun searchMusicWithPagination(query: String): Result<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(100) // Rate limiting

                val searchExtractor = ServiceList.YouTube.getSearchExtractor(query, emptyList(), null)
                searchExtractor.fetchPage()

                val initialPage = searchExtractor.initialPage
                val items = initialPage.items

                val tracks = items
                    .filterIsInstance<StreamInfoItem>()
                    .mapNotNull { streamItem ->
                        try {
                            Track(
                                id = streamItem.url,
                                title = streamItem.name,
                                artist = streamItem.uploaderName ?: "Unknown Artist",
                                thumbnailUrl = streamItem.thumbnails.firstOrNull()?.url ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                Result.success(SearchResult(tracks, initialPage.nextPage))
            } catch (e: Exception) {
                Result.failure(Exception("Search failed: ${e.message}", e))
            }
        }
    }

    // Load more results for pagination
    suspend fun loadMoreResults(query: String, page: Page): Result<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(100) // Rate limiting

                val searchExtractor = ServiceList.YouTube.getSearchExtractor(query, emptyList(), null)

                // Fetch the specific page
                val pageInfo = searchExtractor.getPage(page)
                val items = pageInfo.items

                val tracks = items
                    .filterIsInstance<StreamInfoItem>()
                    .mapNotNull { streamItem ->
                        try {
                            Track(
                                id = streamItem.url,
                                title = streamItem.name,
                                artist = streamItem.uploaderName ?: "Unknown Artist",
                                thumbnailUrl = streamItem.thumbnails.firstOrNull()?.url ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                Result.success(SearchResult(tracks, pageInfo.nextPage))
            } catch (e: Exception) {
                Result.failure(Exception("Loading more results failed: ${e.message}", e))
            }
        }
    }

    override suspend fun getAudioStreamUrl(trackUrl: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, trackUrl)

                val bestAudio = streamInfo.audioStreams
                    .filter {
                        val fmt = it.format
                        fmt != null && fmt.name.contains("M4A", ignoreCase = true)
                    }
                    .maxByOrNull { it.averageBitrate }

                bestAudio?.content?.takeIf { it.isNotEmpty() }?.let {
                    return@withContext Result.success(it)
                }

                val fallback = streamInfo.audioStreams.firstOrNull()?.content
                if (!fallback.isNullOrEmpty()) {
                    return@withContext Result.success(fallback)
                }

                Result.failure(Exception("No audio streams found for this track."))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * More lenient music content filtering for search results
     */
    private fun isMusicContent(streamItem: StreamInfoItem): Boolean {
        val title = streamItem.name.lowercase()
        val uploader = streamItem.uploaderName?.lowercase() ?: ""
        val duration = streamItem.duration

        if (duration < 60) return false

        // Filter out common Shorts indicators in title
        val shortsKeywords = listOf("#shorts", "shorts", "#short")
        if (shortsKeywords.any { title.contains(it) }) return false
        // Music-related keywords in title
        val musicKeywords = listOf(
            "song", "music", "official", "audio", "lyrics", "cover", "remix",
            "ft.", "feat.", "featuring", "mv", "video", "single", "album",
            "track", "hit", "chart", "acoustic", "live", "unplugged", "full song"
        )

        // Music-related keywords in uploader name
        val musicUploaderKeywords = listOf(
            "music", "records", "entertainment", "official", "vevo", "label",
            "sounds", "audio", "productions", "studios", "media", "artists",
            "channel", "fm", "radio"
        )

        // Check title for music keywords
        val hasMusicInTitle = musicKeywords.any { keyword ->
            title.contains(keyword)
        }

        // Check uploader for music keywords
        val hasMusicInUploader = musicUploaderKeywords.any { keyword ->
            uploader.contains(keyword)
        }

        // Duration check (music typically 1-10 minutes)
        val hasReasonableDuration = duration in 60..600 // 1-10 minutes

        // Additional checks for common music patterns
        val hasCommonMusicPatterns = title.contains("|") ||
                title.contains("•") ||
                title.contains("-") ||
                (title.contains("(") && title.contains(")"))

        return (hasMusicInTitle || hasMusicInUploader) &&
                (hasReasonableDuration || hasCommonMusicPatterns)
    }

    /**
     * Very strict music content filtering - only returns content that is very likely to be music
     */
    private fun isStrictMusicContent(streamItem: StreamInfoItem): Boolean {
        val title = streamItem.name.lowercase()
        val uploader = streamItem.uploaderName?.lowercase() ?: ""
        val duration = streamItem.duration

        if (duration < 60) return false

        // Filter out Shorts keywords
        val shortsKeywords = listOf("#shorts", "shorts", "#short")
        if (shortsKeywords.any { title.contains(it) }) return false

        // Strong indicators for music content
        val strongMusicIndicators = listOf(
            "official music video", "official video", "official audio",
            "vevo", "music video", "official mv", "lyrics", "full song",
            "ft.", "feat.", "featuring", "remix", "cover", "acoustic"
        )

        // Music labels and official channels
        val musicChannelIndicators = listOf(
            "vevo", "records", "music", "official", "label", "entertainment",
            "sounds", "audio", "productions", "studios", "media"
        )

        // Check for strong music indicators in title
        val hasStrongMusicInTitle = strongMusicIndicators.any { indicator ->
            title.contains(indicator)
        }

        // Check for music channel indicators in uploader
        val hasMusicChannel = musicChannelIndicators.any { indicator ->
            uploader.contains(indicator)
        }

        // Duration check (music typically 1-8 minutes, stricter than before)
        val hasGoodDuration = duration in 60..480 // 1-8 minutes

        // Check if it's likely a music video based on title patterns
        val hasMusicTitlePattern = (title.contains("-") && !title.contains("tutorial")) ||
                (title.contains("•") && !title.contains("how to")) ||
                (title.contains("|") && !title.contains("gameplay")) ||
                title.matches(Regex(".*\\(.*\\).*")) // Has parentheses which often indicate music

        // Must have at least 2 of these criteria to be considered music
        val criteria = listOf(
            hasStrongMusicInTitle,
            hasMusicChannel,
            hasGoodDuration,
            hasMusicTitlePattern
        )

        return criteria.count { it } >= 2
    }

    /**
     * Get trending music for multiple countries
     */
    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("\\[.*?]"), "") // Remove [Official Video] etc.
            .replace(Regex("\\(.*?Official.*?\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\(.*?Video.*?\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\(.*?Audio.*?\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\|.*"), "") // Remove everything after |
            .replace(Regex("•.*"), "") // Remove everything after •
            .trim()
    }

    suspend fun getRelatedStreams(trackUrl: String): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the stream info to get access to related items
                val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, trackUrl)
                val relatedItems = streamInfo.relatedItems

                val tracks = relatedItems
                    .filterIsInstance<StreamInfoItem>()
                    .mapNotNull { streamItem ->
                        try {
                            Track(
                                id = streamItem.url,
                                title = streamItem.name,
                                artist = streamItem.uploaderName ?: "Unknown Artist",
                                thumbnailUrl = streamItem.thumbnails.firstOrNull()?.url ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get related streams: ${e.message}", e))
            }
        }
    }

    /**
     * Get the best quality thumbnail URL
     */
    private fun getBestThumbnail(thumbnails: List<org.schabi.newpipe.extractor.Image>): String {
        return thumbnails
            .maxByOrNull { it.width * it.height }
            ?.url ?: ""
    }
}

// Data class to hold search results with pagination info
data class SearchResult(
    val tracks: List<Track>,
    val nextPage: Page?
)
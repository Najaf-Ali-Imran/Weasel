
package com.example.weasel.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weasel.data.Suggestion
import com.example.weasel.data.Track
import com.example.weasel.repository.NewPipeMusicRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val tracks: List<Track>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val musicRepository: NewPipeMusicRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set
    var uiState by mutableStateOf<SearchUiState>(SearchUiState.Idle)
        private set
    var suggestions by mutableStateOf<List<Suggestion>>(emptyList())
        private set
    var isLoadingMore by mutableStateOf(false)
        private set

    private var nextPage: Page? = null
    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(300)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { query ->
                fetchSuggestions(query)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchQueryFlow.value = query

        if (query.isEmpty()) {
            uiState = SearchUiState.Idle
            suggestions = emptyList()
            nextPage = null
        }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            if (uiState is SearchUiState.Idle) {
                musicRepository.getSearchSuggestions(query).onSuccess {
                    suggestions = it
                }
            }
        }
    }

    fun search(query: String = searchQuery) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        searchQuery = trimmedQuery
        suggestions = emptyList()

        viewModelScope.launch {
            uiState = SearchUiState.Loading
            nextPage = null
            try {
                val result = musicRepository.searchMusicWithPagination(trimmedQuery)
                result.fold(
                    onSuccess = { searchResult ->
                        nextPage = searchResult.nextPage
                        uiState = SearchUiState.Success(searchResult.tracks)
                    },
                    onFailure = { exception ->
                        uiState = SearchUiState.Error(exception.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                uiState = SearchUiState.Error("Search failed: ${e.message}")
            }
        }
    }
    fun setQueryAndSearch(query: String) {
        onSearchQueryChanged(query)
        search()
    }
    fun loadMoreResults() {
        if (isLoadingMore || nextPage == null) return
        viewModelScope.launch {
            isLoadingMore = true
            try {
                val result = musicRepository.loadMoreResults(searchQuery.trim(), nextPage!!)
                result.fold(
                    onSuccess = { searchResult ->
                        nextPage = searchResult.nextPage
                        val currentTracks = (uiState as? SearchUiState.Success)?.tracks ?: emptyList()
                        uiState = SearchUiState.Success(currentTracks + searchResult.tracks)
                    },
                    onFailure = {
                        nextPage = null
                    }
                )
            } catch (e: Exception) {
                nextPage = null
            } finally {
                isLoadingMore = false
            }
        }
    }
}
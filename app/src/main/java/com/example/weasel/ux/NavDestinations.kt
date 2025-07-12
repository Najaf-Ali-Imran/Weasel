package com.example.weasel.ux

import androidx.annotation.DrawableRes
import com.example.weasel.ui.resources.AppIcons

// Route constants
const val HOME_ROUTE = "home"
const val SEARCH_ROUTE = "search"
const val LIBRARY_ROUTE = "library"
const val PLAYLIST_DETAIL_ROUTE = "playlist_detail"
const val SETTINGS_ROUTE = "settings"
const val LOCAL_SONGS_ROUTE = "local_songs"
const val ADD_SONGS_ROUTE = "add_songs"
const val DOWNLOADS_ROUTE = "downloads"

data class BottomNavItem(
    val label: String,
    @DrawableRes val iconResId: Int,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", AppIcons.NavHome, HOME_ROUTE),
    BottomNavItem("Search", AppIcons.NavSearch, SEARCH_ROUTE),
    BottomNavItem("Library", AppIcons.NavLibrary, LIBRARY_ROUTE)
)
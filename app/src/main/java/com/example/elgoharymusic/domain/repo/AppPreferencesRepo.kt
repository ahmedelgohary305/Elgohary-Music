package com.example.elgoharymusic.domain.repo


import com.example.elgoharymusic.data.repoImpl.AppPreferences
import com.example.elgoharymusic.presentation.viewmodels.SortOrder
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepo {
    suspend fun getSortOrder(): SortOrder
    suspend fun setSortOrder(sortOrder: SortOrder)
    suspend fun getIsDarkTheme(): Boolean
    suspend fun setIsDarkTheme(isDark: Boolean)
    fun getPreferencesFlow(): Flow<AppPreferences>
}
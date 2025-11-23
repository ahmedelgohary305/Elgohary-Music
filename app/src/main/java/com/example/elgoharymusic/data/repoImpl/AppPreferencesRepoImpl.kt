package com.example.elgoharymusic.data.repoImpl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.elgoharymusic.domain.repo.AppPreferencesRepo
import com.example.elgoharymusic.presentation.viewmodels.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppLanguage {
    ENGLISH,
    ARABIC
}

data class AppPreferences(
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val isDarkTheme: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH
)
@Singleton
class AppPreferencesRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferencesRepo {

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override suspend fun getSortOrder(): SortOrder {
        val preferences = context.dataStore.data.first()
        val sortOrderString = preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.TITLE_ASC.name
        return try {
            SortOrder.valueOf(sortOrderString)
        } catch (e: IllegalArgumentException) {
            SortOrder.TITLE_ASC
        }
    }

    override suspend fun setSortOrder(sortOrder: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    override suspend fun getIsDarkTheme(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    override suspend fun setIsDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    override suspend fun getLanguage(): AppLanguage {
        val preferences = context.dataStore.data.first()
        val languageString = preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.ENGLISH.name
        return try {
            AppLanguage.valueOf(languageString)
        } catch (e: IllegalArgumentException) {
            AppLanguage.ENGLISH
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.name
        }
    }

    override fun getPreferencesFlow(): Flow<AppPreferences> {
        return context.dataStore.data.map { preferences ->
            AppPreferences(
                sortOrder = try {
                    SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.TITLE_ASC.name)
                } catch (e: IllegalArgumentException) {
                    SortOrder.TITLE_ASC
                },
                isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: false,
                language = try {
                    AppLanguage.valueOf(preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.ENGLISH.name)
                } catch (e: IllegalArgumentException) {
                    AppLanguage.ENGLISH
                }
            )
        }
    }
}
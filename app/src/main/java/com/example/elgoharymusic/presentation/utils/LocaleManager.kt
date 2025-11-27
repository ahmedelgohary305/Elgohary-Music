package com.example.elgoharymusic.presentation.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {

    enum class Language(val code: String) {
        ENGLISH("en"),
        ARABIC("ar");
    }

    fun setLocale(language: Language) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
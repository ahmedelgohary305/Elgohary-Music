package com.example.elgoharymusic.presentation.utils

import android.content.Context
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeFormatter {

    // Your original duration formatter (enhanced)
    fun formatDuration(milliseconds: Long, language: AppLanguage): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = milliseconds / (1000 * 60 * 60)

        val locale = when (language) {
            AppLanguage.ARABIC -> Locale("ar")
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }

        return if (hours > 0) {
            String.format(locale, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(locale, "%02d:%02d", minutes, seconds)
        }
    }

    // Enhanced duration formatter with text labels
    fun formatDurationWithLabels(milliseconds: Long, context: Context): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = milliseconds / (1000 * 60 * 60)
        val days = hours / 24
        val hoursInDay = hours % 24

        return when {
            days > 0 -> {
                if (hoursInDay > 0) {
                    context.getString(R.string.duration_days_hours_minutes, days, hoursInDay, minutes)
                } else {
                    context.getString(R.string.duration_days_minutes, days, minutes)
                }
            }
            hours > 0 -> context.getString(R.string.duration_hours_minutes, hours, minutes)
            minutes > 0 -> context.getString(R.string.duration_minutes_seconds, minutes, seconds)
            else -> context.getString(R.string.duration_seconds, seconds)
        }
    }

    // Format creation date specifically for playlists
    fun formatPlaylistCreationDate(timestamp: Long, language: AppLanguage, context: Context): String {
        val date = Date(timestamp)
        val now = Date()
        val diffInMillis = now.time - timestamp
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        val locale = when (language) {
            AppLanguage.ARABIC -> Locale("ar")
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }

        return when {
            days == 0L -> context.getString(R.string.created_today)
            days == 1L -> context.getString(R.string.created_yesterday)
            days < 7 -> context.getString(R.string.created_days_ago, days)
            days < 30 -> {
                val weeks = days / 7
                context.getString(R.string.created_weeks_ago, weeks)
            }
            days < 365 -> {
                val months = days / 30
                context.getString(R.string.created_months_ago, months)
            }
            else -> {
                val formatter = SimpleDateFormat("MMM yyyy", locale)
                context.getString(R.string.created_date, formatter.format(date))
            }
        }
    }

    // Format total duration for playlists
    fun formatTotalDuration(totalMilliseconds: Long, songCount: Int, context: Context): String {
        val duration = formatDurationWithLabels(totalMilliseconds, context)
        val songText = context.resources.getQuantityString(R.plurals.song_count, songCount, songCount)
        return "$songText • $duration"
    }

    // Get current timestamp
    fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}

// Extension functions for convenience
fun Long.formatDuration(language: AppLanguage): String =
    TimeFormatter.formatDuration(this, language)

fun Long.formatPlaylistCreation(language: AppLanguage, context: Context): String =
    TimeFormatter.formatPlaylistCreationDate(this, language, context)

fun Int.toLocalizedDigits(language: AppLanguage): String {
    return if (language == AppLanguage.ARABIC) {
        NumberFormat.getInstance(Locale("ar")).format(this)
    } else {
        this.toString()
    }
}
package com.example.elgoharymusic.di

import android.content.Context
import androidx.room.Room
import com.example.elgoharymusic.data.local.SongDatabase
import com.example.elgoharymusic.data.repoImpl.AppPreferencesRepoImpl
import com.example.elgoharymusic.data.repoImpl.FavSongsRepoImpl
import com.example.elgoharymusic.data.repoImpl.MusicRepoImpl
import com.example.elgoharymusic.data.repoImpl.PlaylistRepoImpl
import com.example.elgoharymusic.domain.repo.AppPreferencesRepo
import com.example.elgoharymusic.domain.repo.FavSongsRepo
import com.example.elgoharymusic.domain.repo.MusicRepo
import com.example.elgoharymusic.domain.repo.PlaylistRepo
import com.example.elgoharymusic.presentation.MusicController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MusicModule {

    @Provides
    @Singleton
    fun provideSongDatabase(@ApplicationContext context: Context): SongDatabase {
        return Room.databaseBuilder(
                context,
                SongDatabase::class.java,
                "favSongs_db"
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    fun providePlaylistRepo(
        database: SongDatabase
    ): PlaylistRepo {
        return PlaylistRepoImpl(database)
    }

    @Provides
    fun provideFavSongsRepo(
        database: SongDatabase
    ): FavSongsRepo {
        return FavSongsRepoImpl(database)
    }

    @Provides
    fun provideMusicRepo(
        @ApplicationContext context: Context,
        favSongsRepo: FavSongsRepo,
        playlistRepo: PlaylistRepo
    ): MusicRepo {
        return MusicRepoImpl(context, favSongsRepo, playlistRepo)
    }

    @Provides
    @Singleton
    fun provideMusicController(@ApplicationContext context: Context): MusicController {
        return MusicController(context)
    }

    @Provides
    @Singleton
    fun provideAppPreferencesRepo(
        @ApplicationContext context: Context
    ): AppPreferencesRepo {
        return AppPreferencesRepoImpl(context)
    }

}
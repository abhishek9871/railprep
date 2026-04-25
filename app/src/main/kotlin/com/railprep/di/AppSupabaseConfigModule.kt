package com.railprep.di

import com.railprep.BuildConfig
import com.railprep.data.remote.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bridges the app-level BuildConfig (populated from local.properties) into the data-remote
 * module, which has no BuildConfig of its own.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppSupabaseConfigModule {
    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig = SupabaseConfig(
        url = BuildConfig.SUPABASE_URL,
        anonKey = BuildConfig.SUPABASE_ANON_KEY,
    )
}

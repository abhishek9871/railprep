package com.railprep.data.remote.supabase

import com.railprep.data.remote.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(config: SupabaseConfig): SupabaseClient {
        require(config.url.isNotBlank() && config.anonKey.isNotBlank()) {
            "SUPABASE_URL / SUPABASE_ANON_KEY missing from local.properties — see README."
        }
        return createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey,
        ) {
            install(Auth) {
                // Default storage uses androidx.security EncryptedSharedPreferences under the hood.
            }
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient) = client.auth
}

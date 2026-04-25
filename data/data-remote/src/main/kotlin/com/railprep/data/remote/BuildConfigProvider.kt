package com.railprep.data.remote

/**
 * Supabase URL + anon key are injected into BuildConfig by the application module at build time
 * (read from local.properties). The data-remote module doesn't own a BuildConfig, so the app
 * provides them via this holder through Hilt.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)

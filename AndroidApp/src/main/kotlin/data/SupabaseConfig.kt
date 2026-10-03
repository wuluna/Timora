package cs.uwaterloo.data

import cs.uwaterloo.timora.BuildConfig

/**
 * Supabase configuration constants.
 * Reads credentials from BuildConfig, which loads them from local.properties.
 */
object SupabaseConfig {
    val PROJECT_URL: String = BuildConfig.SUPABASE_URL
    val SERVICE_ROLE_KEY: String = BuildConfig.SUPABASE_KEY
}

package cs.uwaterloo.data.db

import cs.uwaterloo.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Implementation of IDb that connects to Supabase.
 * Provides access to the Supabase client for DAO implementations.
 */
open class SupabaseDb : IDb {
    override val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.PROJECT_URL,
        supabaseKey = SupabaseConfig.SERVICE_ROLE_KEY
    ) {
        install(Postgrest)
    }
}

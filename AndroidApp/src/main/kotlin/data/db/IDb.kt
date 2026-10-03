package cs.uwaterloo.data.db

import io.github.jan.supabase.SupabaseClient

/**
 * Interface for database connection.
 * Provides access to the Supabase client for performing database operations.
 */
interface IDb {
    val client: SupabaseClient
}

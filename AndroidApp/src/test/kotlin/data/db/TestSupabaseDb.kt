package cs.uwaterloo.data.db

import cs.uwaterloo.data.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.cio.CIO

/**
 * Test-specific Supabase database connection that uses CIO engine for JVM tests.
 * This is necessary because the Android engine doesn't work in JUnit tests.
 */
class TestSupabaseDb : IDb {
    override val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.PROJECT_URL,
        supabaseKey = SupabaseConfig.SERVICE_ROLE_KEY
    ) {
        install(Postgrest)

        // Use CIO engine for JVM tests (supports PATCH method)
        httpEngine = CIO.create()
    }
}

package com.chinmaib.sportconnect.di

import com.chinmaib.sportconnect.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

// SUPABASE CONFIGURATION INSTRUCTION:
// Update your Supabase Web Dashboard -> Authentication -> URL Configuration
// Set both "Site URL" and "Redirect URLs" to: sportconnect://login-callback
//
// This is the single, app-wide SupabaseModule. It previously existed as two
// separate competing modules (this one, and a duplicate created for the
// communication feature under com.sportconnect.app.communication.di) which
// caused Dagger/DuplicateBindings errors. They have been merged into this
// one file. The duplicate file must be deleted from the project - see the
// instructions accompanying this file.

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth) {
                // OAuth deep-link catcher. Must match the Site URL / Redirect URLs
                // configured in the Supabase dashboard exactly.
                scheme = "sportconnect"
                host = "login-callback"
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime {
        return client.realtime
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }
}
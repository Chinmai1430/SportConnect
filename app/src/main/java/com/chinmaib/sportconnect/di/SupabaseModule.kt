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
    import io.github.jan.supabase.storage.Storage
    import io.github.jan.supabase.storage.storage
    import javax.inject.Singleton

    // SUPABASE CONFIGURATION INSTRUCTION:
    // Update your Supabase Web Dashboard -> Authentication -> URL Configuration
    // Set both "Site URL" and "Redirect URLs" to: sportconnect://login-callback

    @Module
    @InstallIn(SingletonComponent::class)
    @Suppress("unused")
    object SupabaseModule {

        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
            ) {
                install(Auth) {
                    // ALTRON OAUTH CATCHER: Configured to intercept Google deep links
                    scheme = "sportconnect"
                    host = "login-callback"
                }
                install(Postgrest)
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
        fun provideSupabaseDatabase(client: SupabaseClient): Postgrest {
            return client.postgrest
        }

        @Provides
        @Singleton
        fun provideSupabaseStorage(client: SupabaseClient): Storage {
            return client.storage
        }
    }
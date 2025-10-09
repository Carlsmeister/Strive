package se.umu.calu0217.strive.core.dependencyInjection

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import se.umu.calu0217.strive.BuildConfig
import se.umu.calu0217.strive.data.remote.ExerciseApiService
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for network components.
 * Provides singleton instances of Retrofit, OkHttpClient, and API services.
 * Configures API authentication and JSON serialization.
 * @author Carl Lundholm
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides JSON serializer configuration.
     * Ignores unknown keys and coerces input values for flexible API responses.
     * @return Configured Json instance.
     * @author Carl Lundholm
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Provides OkHttpClient with logging and API key authentication.
     * Adds RapidAPI headers for ExerciseDB API access.
     * Enables HTTP logging in debug builds.
     * @return Configured OkHttpClient instance.
     * @author Carl Lundholm
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", BuildConfig.RAPIDAPI_KEY)
                    .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Provides Retrofit instance for API communication.
     * Configured for ExerciseDB API with JSON serialization.
     * @param okHttpClient The HTTP client with authentication.
     * @param json The JSON serializer configuration.
     * @return Configured Retrofit instance.
     * @author Carl Lundholm
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://exercisedb.p.rapidapi.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Provides ExerciseApiService for exercise data API calls.
     * @param retrofit The configured Retrofit instance.
     * @return ExerciseApiService implementation.
     * @author Carl Lundholm
     */
    @Provides
    @Singleton
    fun provideExerciseApiService(retrofit: Retrofit): ExerciseApiService {
        return retrofit.create(ExerciseApiService::class.java)
    }
}

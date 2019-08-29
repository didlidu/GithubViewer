package com.bunjlabs.githubviewer.model

import com.bunjlabs.githubviewer.model.entity.SearchResultEntity
import com.bunjlabs.githubviewer.model.entity.UserEntity
import com.bunjlabs.githubviewer.model.repository.AuthRepository
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit


class AuthenticationInterceptor(private val authRepository: AuthRepository) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (authRepository.getAuthEntity().value != null && authRepository.getAuthEntity().value?.isAnonymous == false) {
            val credentials: String = Credentials.basic(
                authRepository.getAuthEntity().value?.login ?: "",
                authRepository.getAuthEntity().value?.password ?: ""
            )
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(request)
        }
    }
}

class SimpleAuthenticationInterceptor(private val login: String, private val password: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder()
            .header("Authorization", Credentials.basic(login, password)).build())
    }
}

interface GithubApi {
    @GET("user")
    fun getCurrentUser(): Call<UserEntity>

    @GET("search/repositories")
    fun searchRepositories(@Query("q") query: String, @Query("page") page: Int): Call<SearchResultEntity>

    companion object Factory {

        private const val API_URL = "https://api.github.com/"

        private fun create(authInterceptor: Interceptor): GithubApi {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(GithubApi::class.java)
        }

        fun create(authRepository: AuthRepository): GithubApi {
            return create(AuthenticationInterceptor(authRepository))
        }

        fun create(login: String, password: String): GithubApi {
            return create(SimpleAuthenticationInterceptor(login, password))
        }
    }
}
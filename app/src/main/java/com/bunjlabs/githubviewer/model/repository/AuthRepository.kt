package com.bunjlabs.githubviewer.model.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bunjlabs.githubviewer.model.GithubApi
import com.bunjlabs.githubviewer.model.entity.AuthEntity
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.model.entity.UserEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository private constructor(appContext: Context) {

    private val sharedPref: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val observableAuthEntity: MediatorLiveData<AuthEntity> = MediatorLiveData()
    private val observableRepositoryState: MutableLiveData<RepositoryState> = MutableLiveData()

    init {
        observableAuthEntity.value = parseAuthEntity(sharedPref.getString(AUTH_KEY_NAME, null))
        /*sharedPref.registerOnSharedPreferenceChangeListener { sharedPref, key ->
            if (key == AUTH_KEY_NAME) {
                observableAuthEntity.postValue(parseAuthEntity(sharedPref.getString(AUTH_KEY_NAME, null)))
            }
        }*/
    }

    fun getAuthEntity(): LiveData<AuthEntity> {
        return observableAuthEntity
    }

    fun getRepositoryState(): LiveData<RepositoryState> {
        return observableRepositoryState
    }

    private fun parseAuthEntity(value: String?): AuthEntity? {
        return when (value) {
            null -> null
            "" -> null
            "anonymous" -> AuthEntity("", "", true)
            else -> {
                val arr = value.split("|")
                val login = arr[0]
                val password = arr[1]
                AuthEntity(login, password, false)
            }
        }
    }

    private fun saveAuthEntity(authEntity: AuthEntity?) {
        val editor = sharedPref.edit()
        when {
            authEntity == null -> editor.putString(AUTH_KEY_NAME, null)
            authEntity.isAnonymous -> editor.putString(AUTH_KEY_NAME, "anonymous")
            else -> editor.putString(AUTH_KEY_NAME, authEntity.login + "|" + authEntity.password)
        }
        editor.apply()
    }

    fun auth(login: String, password: String) {
        observableRepositoryState.value = RepositoryState.LOADING

        GithubApi.create(login, password).getCurrentUser().enqueue(object : Callback<UserEntity> {
            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                if (response.isSuccessful) {
                    observableRepositoryState.value = null
                    val authEntity = AuthEntity(login, password,false)
                    saveAuthEntity(authEntity)
                    observableAuthEntity.value = authEntity
                } else {
                    if (response.code() == 401) {
                        observableRepositoryState.value = RepositoryState.BAD_CREDENTIALS
                    } else {
                        observableRepositoryState.value = RepositoryState.API_ERROR
                    }
                }
            }

            override fun onFailure(call: Call<UserEntity>, t: Throwable) {
                observableRepositoryState.value = RepositoryState.NET_ERROR
            }
        })
    }

    fun continueAsAnonymous() {
        val authEntity = AuthEntity("", "",true)
        saveAuthEntity(authEntity)
        observableAuthEntity.value = authEntity
    }

    fun logout() {
        val authEntity = null
        saveAuthEntity(authEntity)
        observableAuthEntity.value = authEntity
    }

    companion object {
        private const val PREFS_NAME = "auth_repository_shared_preferences"
        private const val AUTH_KEY_NAME = "auth"
        private var instance: AuthRepository? = null

        fun getInstance(appContext: Context): AuthRepository {
            if (instance == null) {
                synchronized(AuthRepository::class) {
                    if (instance == null) {
                        instance = AuthRepository(appContext)
                    }
                }
            } else {
                instance?.observableRepositoryState?.value = null
            }
            return instance!!
        }
    }

}
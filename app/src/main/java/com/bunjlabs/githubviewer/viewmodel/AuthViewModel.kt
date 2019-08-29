package com.bunjlabs.githubviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.bunjlabs.githubviewer.model.entity.AuthEntity
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.model.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    var cachedLogin = ""
    var cachedPassword = ""

    private val repository: AuthRepository = AuthRepository.getInstance(application.applicationContext)

    private val observableAuthEntity: MediatorLiveData<AuthEntity> = MediatorLiveData()
    private val observableRepositoryState: MediatorLiveData<RepositoryState> = MediatorLiveData()

    init {
        observableAuthEntity.addSource(repository.getAuthEntity(), observableAuthEntity::setValue)
        observableRepositoryState.addSource(repository.getRepositoryState(), observableRepositoryState::setValue)
    }

    fun auth(login: String, password: String) {
        repository.auth(login, password)
    }

    fun continueAsAnonymous() {
        repository.continueAsAnonymous()
    }

    fun logout() {
        repository.logout()
    }

    fun getAuthEntity(): LiveData<AuthEntity> {
        return observableAuthEntity
    }

    fun getRepositoryState(): LiveData<RepositoryState> {
        return observableRepositoryState
    }

}

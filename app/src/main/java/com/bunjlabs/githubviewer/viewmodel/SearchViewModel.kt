package com.bunjlabs.githubviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.bunjlabs.githubviewer.model.entity.RepositoryEntity
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.model.repository.SearchRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SearchRepository = SearchRepository.getInstance(application.applicationContext)

    private val observableRepositories: MediatorLiveData<List<RepositoryListItem>> = MediatorLiveData()
    private val observableRepositoryState: MediatorLiveData<RepositoryState> = MediatorLiveData()

    init {
        observableRepositories.addSource(repository.getSearchResults(), Observer { searchResults ->
            val list = ArrayList<RepositoryListItem>()

            if (searchResults != null) {
                for (searchResult in searchResults) {
                    for (repository in searchResult.items) {
                        list.add(RepositoryListItem(false, repository))
                    }
                }
            }

            if (observableRepositoryState.value == RepositoryState.LOADING) {
                list.add(RepositoryListItem(true, null))
            }

            if (list.isEmpty()) {
                observableRepositories.value = null
            } else {
                observableRepositories.value = list
            }
        })
        observableRepositoryState.addSource(repository.getRepositoryState(), observableRepositoryState::setValue)
    }

    fun getRepositoryState(): LiveData<RepositoryState> {
        return observableRepositoryState
    }

    fun getRepositories(): LiveData<List<RepositoryListItem>> {
        return observableRepositories
    }

    fun getQuery(): String {
        return repository.getQuery()
    }

    fun search(query: String) {
        repository.search(query.trim())
    }

    fun searchNext() {
        repository.searchNext()
    }

}

data class RepositoryListItem(
    val isLoadingItem: Boolean,
    val repository: RepositoryEntity?
)

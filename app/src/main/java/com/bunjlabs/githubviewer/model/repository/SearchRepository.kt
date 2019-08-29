package com.bunjlabs.githubviewer.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bunjlabs.githubviewer.model.GithubApi
import com.bunjlabs.githubviewer.model.entity.RepositoryState
import com.bunjlabs.githubviewer.model.entity.SearchResultEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class SearchRepository private constructor(appContext: Context) {

    private val api: GithubApi = GithubApi.create(AuthRepository.getInstance(appContext))

    private var query: String = ""
    private var maxCount = 0
    private var count = 0
    private var lastErrorTime: Long = 0
    private var observableSearchResults: MediatorLiveData<ArrayList<SearchResultEntity>> = MediatorLiveData()
    private val observableRepositoryState: MutableLiveData<RepositoryState> = MutableLiveData()

    private var networkCall: Call<SearchResultEntity>? = null

    fun getSearchResults(): LiveData<ArrayList<SearchResultEntity>> {
        return observableSearchResults
    }

    fun getRepositoryState(): LiveData<RepositoryState> {
        return observableRepositoryState
    }

    fun getQuery(): String {
        return query
    }

    fun search(query: String) {
        if (this.query == query && observableSearchResults.value?.isEmpty() == false) return

        this.query = query
        this.maxCount = 0
        this.count = 0

        callSearchRequest(query, 1)
        observableSearchResults.value = null
    }

    fun searchNext() {
        if (Date().time - lastErrorTime < 3 * 1000
            || query.isEmpty()
            || observableSearchResults.value?.isEmpty() != false
            || count >= maxCount
            || observableRepositoryState.value == RepositoryState.LOADING) {
            return
        }
        callSearchRequest(query, observableSearchResults.value!!.size + 1)
        observableSearchResults.value = observableSearchResults.value
    }

    private fun callSearchRequest(query: String, page: Int) {
        if (networkCall?.isExecuted == false && networkCall?.isCanceled == false) networkCall?.cancel()
        observableRepositoryState.value = RepositoryState.LOADING
        networkCall = api.searchRepositories(query, page)
        networkCall?.enqueue(object : Callback<SearchResultEntity> {
            override fun onResponse(call: Call<SearchResultEntity>, response: Response<SearchResultEntity>) {
                if (response.isSuccessful) {
                    observableRepositoryState.value = null
                    val list: ArrayList<SearchResultEntity>
                    if (observableSearchResults.value == null) {
                        list = ArrayList()
                        list.add(response.body()!!)
                    } else {
                        list = observableSearchResults.value!!
                        list.add(response.body()!!)
                    }
                    maxCount = response.body()!!.total
                    count += response.body()!!.items.size
                    observableSearchResults.value = list
                } else {
                    lastErrorTime = Date().time
                    if (response.code() == 403) {
                        observableRepositoryState.value = RepositoryState.REQUEST_LIMIT_EXCEEDED
                    } else {
                        observableRepositoryState.value = RepositoryState.API_ERROR
                    }
                    observableSearchResults.value = observableSearchResults.value
                }
            }

            override fun onFailure(call: Call<SearchResultEntity>, t: Throwable) {
                lastErrorTime = Date().time
                observableRepositoryState.value = RepositoryState.NET_ERROR
            }
        })
    }

    companion object {
        private var instance: SearchRepository? = null

        fun getInstance(appContext: Context): SearchRepository {
            if (instance == null) {
                synchronized(SearchRepository::class) {
                    if (instance == null) {
                        instance = SearchRepository(appContext)
                    }
                }
            } else {
                instance?.observableRepositoryState?.value = null
            }
            return instance!!
        }
    }

}
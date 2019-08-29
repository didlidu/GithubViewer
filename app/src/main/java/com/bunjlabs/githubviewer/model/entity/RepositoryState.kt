package com.bunjlabs.githubviewer.model.entity

import com.bunjlabs.githubviewer.R

enum class RepositoryState {
    LOADING,
    NET_ERROR,
    API_ERROR,
    REQUEST_LIMIT_EXCEEDED,
    BAD_CREDENTIALS;

    fun isError(): Boolean {
        return this == NET_ERROR || this == API_ERROR || this == REQUEST_LIMIT_EXCEEDED || this == BAD_CREDENTIALS
    }

    fun getErrorMessageResId(): Int {
        return when (this) {
            API_ERROR -> R.string.api_error_snackbar_message
            NET_ERROR -> R.string.net_error_snackbar_message
            REQUEST_LIMIT_EXCEEDED -> R.string.request_limit_exceeded_error
            BAD_CREDENTIALS -> R.string.bad_credentials_error
            else -> throw IllegalArgumentException("Invalid error type $this")
        }
    }
}
package com.bunjlabs.githubviewer.model.entity

data class AuthEntity(
    val login: String,
    val password: String,
    val isAnonymous: Boolean
)
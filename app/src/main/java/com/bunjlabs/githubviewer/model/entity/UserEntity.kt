package com.bunjlabs.githubviewer.model.entity

import com.google.gson.annotations.SerializedName

data class UserEntity(
    val id: Int,
    val login: String,
    val name: String,
    @SerializedName("avatar_url") val avatarUrl: String?
)
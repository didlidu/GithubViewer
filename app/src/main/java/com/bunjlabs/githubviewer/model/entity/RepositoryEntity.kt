package com.bunjlabs.githubviewer.model.entity

import com.google.gson.annotations.SerializedName

data class RepositoryEntity(
    val id: Int,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    val description: String?,
    val owner: UserEntity
)
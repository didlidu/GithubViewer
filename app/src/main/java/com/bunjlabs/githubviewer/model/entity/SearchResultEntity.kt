package com.bunjlabs.githubviewer.model.entity

import com.google.gson.annotations.SerializedName

data class SearchResultEntity(
    @SerializedName("total_count") val total: Int,
    @SerializedName("incomplete_results") val incompleteResults: Boolean,
    @SerializedName("items") val items: List<RepositoryEntity>
)
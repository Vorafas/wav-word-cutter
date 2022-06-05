package com.example.wordcutter.model

import com.google.gson.annotations.SerializedName

data class PartialResult(
    @SerializedName("partial")
    val text: String
)
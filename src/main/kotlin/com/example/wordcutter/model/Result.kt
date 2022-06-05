package com.example.wordcutter.model

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("result")
    val words: List<WordInfo>,
    val text: String
)
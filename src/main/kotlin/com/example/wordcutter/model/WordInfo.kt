package com.example.wordcutter.model

import com.google.gson.annotations.SerializedName

data class WordInfo(
    @SerializedName("conf")
    val confidence: Double,
    @SerializedName("end")
    val endTime: Double,
    @SerializedName("start")
    val startTime: Double,
    val word: String
)
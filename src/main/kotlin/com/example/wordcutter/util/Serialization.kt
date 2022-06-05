package com.example.wordcutter.util

import com.google.gson.Gson

object Serialization {

    private val gson = Gson()

    fun <T> toJson(t: T): String = gson.toJson(t)

    fun <T> fromJson(json: String, classOfT: Class<T>): T = gson.fromJson(json, classOfT)
}
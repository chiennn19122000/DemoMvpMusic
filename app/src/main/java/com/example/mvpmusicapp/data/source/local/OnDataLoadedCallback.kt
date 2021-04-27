package com.example.mvpmusicapp.data.source.local

interface OnDataLoadedCallback<T> {
    fun onSuccess(data: T)
    fun onFailure(message: String)
}

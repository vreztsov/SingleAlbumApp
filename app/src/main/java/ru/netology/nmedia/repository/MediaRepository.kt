package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Album

interface MediaRepository {
    fun getTrackUrl(trackName: String): String
    fun getAlbumAsync(callback: Callback<Album>)

    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }
}
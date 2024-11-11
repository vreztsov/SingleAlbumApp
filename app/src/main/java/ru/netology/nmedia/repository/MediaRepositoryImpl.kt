package ru.netology.nmedia.repository

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.netology.nmedia.BuildConfig.DATA_URL
import ru.netology.nmedia.dto.Album
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaRepositoryImpl : MediaRepository {

    private val gson = Gson()
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun getTrackUrl(trackName: String) = "$DATA_URL/$trackName"

    override fun getAlbumAsync(callback: MediaRepository.Callback<Album>) {
        val request: Request = Request.Builder()
            .url("$DATA_URL/album.json")
            .build()

        okHttpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(gson.fromJson(body, Album::class.java))
                    } catch (e: Exception) {
                        throw RuntimeException("Альбом не загружен")
                    }
                }
            })
    }
}
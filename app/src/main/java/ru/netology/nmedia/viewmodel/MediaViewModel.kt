package ru.netology.nmedia.viewmodel

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nmedia.dto.Album
import ru.netology.nmedia.dto.Track
import ru.netology.nmedia.repository.MediaRepository
import ru.netology.nmedia.repository.MediaRepositoryImpl

class MediaViewModel(
    private val mediaPlayer: MediaPlayer
) : ViewModel() {
    private val repository: MediaRepository = MediaRepositoryImpl()
    private val _album = MutableLiveData<Album?>(null)
    val album: LiveData<Album?>
        get() = _album

    val isPlaying
        get() = flow {
            while (true) {
                emit(mediaPlayer.isPlaying)
                delay(25)
            }
        }


    val dataAlbumTrack
        get() = flow {
            while (true) {
                emit(_dataAlbumTracks)
                delay(25)
            }
        }
    private var _dataAlbumTracks: List<Track> = listOf()
    private val currentTrack = MutableLiveData<Track?>(null)

    val track: LiveData<Track?>
        get() = currentTrack
    val trackDuration: Flow<Int>
        get() = flow {
            while (true) {
                emit(mediaPlayer.duration)
                delay(25)
            }
        }

    val trackCurrentPosition
        get() = flow {
            while (true) {
                emit(mediaPlayer.currentPosition)
                delay(25)
            }
        }

    init {
        loadAlbum()
    }

    private fun loadAlbum() {
        repository.getAlbumAsync(
            object : MediaRepository.Callback<Album> {
                override fun onSuccess(result: Album) {
                    _album.postValue(result)
                    _dataAlbumTracks = result.tracks
                }

                override fun onError(e: Exception) {
                    throw RuntimeException("Альбом не загружен")
                }
            }
        )
    }

    fun onPlayItem(track: Track) {
        mediaPlayer.pause()
        _dataAlbumTracks = _dataAlbumTracks.map {
            it.copy(isPlaying = false)
        }
        if (currentTrack.value?.id != track.id) {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(repository.getTrackUrl(track.file))
            mediaPlayer.prepare()
            _dataAlbumTracks = _dataAlbumTracks.map {
                if (it.id == track.id) {
                    currentTrack.value = it
                    val minuteDur = mediaPlayer.duration / 60_000
                    val secondsDur = (mediaPlayer.duration / 1_000) - (minuteDur * 60)
                    it.copy(
                        isPlaying = true,
                        duration = "$minuteDur:${if (secondsDur < 10) "0" else ""}$secondsDur"
                    )
                } else {
                    it
                }
            }
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                val thisTrackInData = _dataAlbumTracks.find {
                    it.id == track.id
                }
                val thisTrackIndex = _dataAlbumTracks.indexOf(thisTrackInData)
                val lastIndex = _dataAlbumTracks.lastIndex
                if (lastIndex == thisTrackIndex) {
                    onPlayItem(_dataAlbumTracks[0])
                } else {
                    onPlayItem(_dataAlbumTracks[thisTrackIndex + 1])
                }
            }
        }
    }
}

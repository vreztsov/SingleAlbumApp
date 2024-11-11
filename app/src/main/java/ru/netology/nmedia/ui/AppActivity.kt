package ru.netology.nmedia.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapters.OnInteractionListener
import ru.netology.nmedia.adapters.TrackAdapter
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.dto.Track
import ru.netology.nmedia.viewmodel.MediaViewModel

class AppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppBinding
    private lateinit var adapter: TrackAdapter
    private val mediaPlayer = MediaPlayer()
    private val viewModel = MediaViewModel(mediaPlayer)
    private val interactionListener = object : OnInteractionListener {
        override fun switch(track: Track) {
            viewModel.onPlayItem(track)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = TrackAdapter(interactionListener)
        binding.listItem.adapter = adapter
        subscribe()
        flowData()
        setListeners()
    }

    private fun subscribe() {
        viewModel.album.observe(this) {
            it?.let {
                binding.apply {
                    album.text = it.title
                    albumName.text = it.subtitle
                    artist.text = it.artist
                    yearName.text = it.published
                    genreName.text = it.genre
                }
                it.tracks.let { trackList ->
                    adapter.submitList(trackList)
                }
            }
        }

        viewModel.track.observe(this) {
            binding.currentTrack.text = it?.file ?: ""
        }
    }

    private fun flowData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isPlaying.collectLatest {
                    binding.playAlbum.isChecked = it
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.dataAlbumTrack.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.trackCurrentPosition.collectLatest {
                    if (it in 1..Int.MAX_VALUE) {
                        val minuteDur = it / 60_000
                        val secondsDur = (it / 1_000) - (minuteDur * 60)
                        val currentPosition =
                            "$minuteDur:${if (secondsDur < 10) "0" else ""}$secondsDur"
                        findViewById<TextView>(R.id.current_position).text = currentPosition
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.trackDuration.collectLatest {
                    if (it in 1..Int.MAX_VALUE) {
                        val minuteDur = it / 60_000
                        val secondsDur = (it / 1_000) - (minuteDur * 60)
                        val duration =
                            " / $minuteDur:${if (secondsDur < 10) "0" else ""}$secondsDur"
                        findViewById<TextView>(R.id.duration).text = duration
                    }
                }
            }
        }
    }

    private fun setListeners() {
        findViewById<MaterialButton>(R.id.play_album).setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
        }
    }
//
//    private fun requestData() {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url("$DATA_URL/album.json")
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("AppActivity", "Error fetching JSON")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val jsonString = response.body?.string()
//                val album = Gson().fromJson(jsonString, Album::class.java)
//                val albumTracks = album.tracks
//                viewModel.setAlbumTracks(albumTracks)
//                runOnUiThread {
//                    findViewById<TextView>(R.id.album_name).text = album.title
//                    findViewById<TextView>(R.id.artist_name).text = album.artist
//                    findViewById<TextView>(R.id.genre_name).text = album.genre
//                    findViewById<TextView>(R.id.year_name).text = album.published
//                }
//            }
//        })
//    }
}
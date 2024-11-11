package ru.netology.nmedia.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemSoundBinding
import ru.netology.nmedia.dto.Track

interface OnInteractionListener {
    fun switch(track: Track)
}

class TrackAdapter(
    private val interactionListener: OnInteractionListener
) : ListAdapter<Track, TrackViewHolder>(TracksDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding =
            ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding, interactionListener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }



}

class TracksDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}

class TrackViewHolder(
    private val binding: ItemSoundBinding,
    private val interactionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {
        binding.apply {
            trackName.text = track.file
            binding.playItem.isChecked = track.isPlaying
            trackTime.text = track.duration ?: ""
        }
        setListeners(track)
    }

    private fun setListeners(track: Track) = with(binding) {
        playItem.setOnClickListener {
            interactionListener.switch(track)
        }
    }
}
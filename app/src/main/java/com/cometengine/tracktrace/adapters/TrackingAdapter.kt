package com.cometengine.tracktrace.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometengine.tracktrace.R
import com.cometengine.tracktrace.database.TrackingItemDescription
import com.cometengine.tracktrace.databinding.TrackingItemBinding
import com.cometengine.tracktrace.misc.cancelNotification
import com.cometengine.tracktrace.viewmodels.TrackingItemViewModel

class TrackingAdapter(private var clickListener: ClickListener) :
    SelectableListAdapter<TrackingItemDescription, TrackingAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackingItemDescription>() {

            override fun areItemsTheSame(oldConcert: TrackingItemDescription, newConcert: TrackingItemDescription): Boolean =
                oldConcert.id == newConcert.id && oldConcert.trackingId == newConcert.trackingId

            override fun areContentsTheSame(oldConcert: TrackingItemDescription, newConcert: TrackingItemDescription): Boolean =
                oldConcert == newConcert
        }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.tracking_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, data: TrackingItemDescription, position: Int) {
        with(holder) { bind(data) }
    }

    override fun getPositionId(position: Int): String = getData(position).trackingId

    inner class ViewHolder(private val binding: TrackingItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        fun bind(trackingItem: TrackingItemDescription) {

            cancelNotification(trackingItem.trackingId)

            binding.viewModel = TrackingItemViewModel(trackingItem)
            binding.executePendingBindings()
        }

        init {
            binding.layout.setOnClickListener(this)
            binding.options.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            when (p0.id) {
                R.id.layout -> clickListener.onItemClicked(getData(adapterPosition))
                R.id.options -> clickListener.onOptionsClicked(getData(adapterPosition))
            }
        }
    }

    interface ClickListener {
        fun onItemClicked(trackingItem: TrackingItemDescription)
        fun onOptionsClicked(trackingItem: TrackingItemDescription)
    }
}
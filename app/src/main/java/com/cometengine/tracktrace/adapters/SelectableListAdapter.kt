package com.cometengine.tracktrace.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class SelectableListAdapter<T, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, VH>(diffCallback) {

    private var selectedItems: ArrayList<String> = ArrayList()
    private var active: Boolean = false

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    fun isSelected(id: String): Boolean {
        return selectedItems.contains(id)
    }

    fun isSelectActive(): Boolean {
        return active
    }

    fun addSelected(id: String) {
        selectedItems.add(id)
    }

    fun toggleSelection(position: Int, id: String) {

        active = true

        if (selectedItems.contains(id)) {
            selectedItems.remove(id)
        } else {
            selectedItems.add(id)
        }

        if (selectedItems.isEmpty()) {
            clearSelection()
        } else {
            notifyItemChanged(position)
        }
    }

    fun clearSelection() {
        active = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelected(): ArrayList<String> {
        return selectedItems
    }

    fun getData(position: Int): T = getItem(position)

    fun getNext(position: Int): T? {
        val next = position + 1
        return if (itemCount == next) {
            null
        } else {
            getItem(next)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(inflater, parent, viewType)
    }

    abstract fun onBindViewHolder(holder: VH, data: T, position: Int)

    abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH

    abstract fun getPositionId(position: Int): String
}
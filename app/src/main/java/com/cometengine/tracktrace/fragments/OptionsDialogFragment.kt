package com.cometengine.tracktrace.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometengine.tracktrace.R
import com.cometengine.tracktrace.adapters.SelectableListAdapter
import com.cometengine.tracktrace.databinding.OptionsDialogBinding
import com.cometengine.tracktrace.databinding.OptionsItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.ArrayList

class OptionsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = OptionsDialogFragment::class.java.simpleName

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldConcert: Int, newConcert: Int): Boolean =
                oldConcert == newConcert

            override fun areContentsTheSame(oldConcert: Int, newConcert: Int): Boolean =
                oldConcert == newConcert
        }

        @JvmStatic
        private val ARG_OPTIONS = "ARG_OPTIONS"

        @JvmStatic
        fun newInstance(options: ArrayList<Int>, listener: ClickListener): OptionsDialogFragment =
            OptionsDialogFragment().apply {
                this.listener = listener
                arguments = Bundle().apply {
                    putIntegerArrayList(ARG_OPTIONS, options)
                }
            }
    }

    private var listener: ClickListener? = null

    private val adapter = OptionsAdapter()

    private lateinit var binding: OptionsDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = OptionsDialogBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean = false
        }

        binding.recyclerView.adapter = adapter

        arguments?.also {
            adapter.submitList(it.getIntegerArrayList(ARG_OPTIONS))
        }

        return binding.root
    }

    inner class OptionsAdapter : SelectableListAdapter<Int, OptionsAdapter.ViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.options_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, data: Int, position: Int) {
            holder.binding.title.setText(data)
        }

        override fun getPositionId(position: Int): String = getData(position).toString()

        inner class ViewHolder(val binding: OptionsItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

            init {
                binding.action.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                listener?.onOptionSelected(adapterPosition)
                dismissAllowingStateLoss()
            }
        }
    }

    interface ClickListener {
        fun onOptionSelected(position: Int)
    }
}
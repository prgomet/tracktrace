package com.cometengine.tracktrace.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.cometengine.tracktrace.database.InsertTrackingItem
import com.cometengine.tracktrace.database.TrackingItem
import com.cometengine.tracktrace.database.TrackingItem.Companion.TRANSIT
import com.cometengine.tracktrace.databinding.AddNewItemDialogBinding
import com.cometengine.tracktrace.misc.currentTimeStamp
import java.util.regex.Pattern

class AddNewItemDialogFragment : DialogFragment() {

    companion object {
        val TAG: String = AddNewItemDialogFragment::class.java.simpleName

        val pattern: Pattern = Pattern.compile("([a-zA-Z]{2}[0-9]{9}[a-zA-Z]{2})", Pattern.CASE_INSENSITIVE)

        fun newInstance() = AddNewItemDialogFragment()
    }

    private lateinit var imm: InputMethodManager

    private lateinit var binding: AddNewItemDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddNewItemDialogBinding.inflate(inflater, container, false)

        binding.save.setOnClickListener { saveData() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.editNumber) {
            requestFocus()
            post { imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT) }
        }
    }

    private fun saveData() {

        val title = binding.editTitle.text?.trim().toString()
        val trackingId = binding.editNumber.text?.trim().toString()

        if (trackingId.isNotEmpty()) {

            val trackingItem = TrackingItem()

            val ts = currentTimeStamp

            trackingItem.id = trackingId
            trackingItem.title = title
            trackingItem.status = TRANSIT
            trackingItem.created = ts
            trackingItem.upd = ts

            InsertTrackingItem().execute(trackingItem)

            closeWindow()
        }
    }

    private fun closeWindow() {
        dialog?.window?.currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
        dismissAllowingStateLoss()
    }
}
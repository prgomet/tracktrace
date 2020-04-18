package com.cometengine.tracktrace.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.cometengine.tracktrace.database.TrackingItemDescription
import com.cometengine.tracktrace.database.UpdateTrackingItemTitle
import com.cometengine.tracktrace.databinding.EditTitleDialogBinding

class EditTitleDialogFragment : DialogFragment() {

    companion object {
        val TAG: String = EditTitleDialogFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(trackingItemDescription: TrackingItemDescription) = EditTitleDialogFragment().apply {
            this.trackingItemDescription = trackingItemDescription
        }
    }

    private lateinit var imm: InputMethodManager

    private lateinit var trackingItemDescription: TrackingItemDescription

    private lateinit var binding: EditTitleDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = EditTitleDialogBinding.inflate(inflater, container, false)

        binding.editNumber.text = Editable.Factory().newEditable(trackingItemDescription.trackingId)
        binding.editTitle.text = Editable.Factory().newEditable(trackingItemDescription.title)

        binding.save.setOnClickListener { saveData() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.editTitle) {
            requestFocus()
            post { imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT) }
        }
    }

    private fun saveData() {

        val title = binding.editTitle.text?.trim().toString()

        if (title != trackingItemDescription.title) {

            val trackingId = trackingItemDescription.trackingId

            UpdateTrackingItemTitle(trackingId).execute(title)

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
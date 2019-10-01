package com.cometengine.tracktrace.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cometengine.tracktrace.AppInit
import com.cometengine.tracktrace.R
import com.cometengine.tracktrace.adapters.TrackingAdapter
import com.cometengine.tracktrace.database.ChangeStatus
import com.cometengine.tracktrace.database.DeleteTrackingItem
import com.cometengine.tracktrace.database.TrackingItem.Companion.DELIVERED
import com.cometengine.tracktrace.database.TrackingItem.Companion.TRANSIT
import com.cometengine.tracktrace.database.TrackingItemDescription
import com.cometengine.tracktrace.databinding.MainFragmentBinding
import com.cometengine.tracktrace.misc.dp
import com.cometengine.tracktrace.misc.fadeInAndMoveY
import com.cometengine.tracktrace.misc.fadeOutAndMoveY
import com.cometengine.tracktrace.viewmodels.MainViewModel
import com.cometengine.tracktrace.viewmodels.TrackingItemViewModel
import org.jetbrains.anko.doAsync

class MainFragment : Fragment(),
    TrackingAdapter.ClickListener,
    Toolbar.OnMenuItemClickListener {

    companion object {
        val TAG: String = MainFragment::class.java.simpleName

        fun newInstance() = MainFragment()
    }

    private var scrollToBottom = false

    private val adapter = TrackingAdapter(this)

    private lateinit var clipboard: ClipboardManager

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: MainFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(activity!!)
            .get(MainViewModel::class.java)

        clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)

        binding.hasItems = true

        binding.recyclerView.adapter = adapter

        binding.bottomAppBar.setOnMenuItemClickListener(this)

        binding.fab.setOnClickListener {
            AddNewItemDialogFragment.newInstance().show(childFragmentManager, TAG)
        }

        binding.close.setOnClickListener {
            viewModel.query.value = null
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.list.observe(viewLifecycleOwner, Observer { list ->
            val hasItems = list != null && list.isNotEmpty()
            if (hasItems != binding.hasItems) {
                binding.hasItems = hasItems
            }
            adapter.submitList(list) {
                doAsync { viewModel.updateSeen() }

                if (scrollToBottom) {
                    scrollToBottom = false
                    with(binding.recyclerView) {
                        post {
                            smoothScrollToPosition(0)
                        }
                    }
                }
            }
        })

        viewModel.query.observe(viewLifecycleOwner, Observer { trackingItem ->
            if (trackingItem != null) {

                binding.fab.hide()
                binding.appbar.fadeInAndMoveY()

                val model = TrackingItemViewModel(trackingItem)
                binding.statusIcon.setImageResource(model.statusIcon)
                binding.title.text = model.title
                binding.tracking.text = model.tracking

                with(binding.recyclerView) {
                    run {
                        val params = this.layoutParams as ViewGroup.MarginLayoutParams
                        params.bottomMargin = 0
                        this.layoutParams = params
                    }
                }
            } else {

                binding.fab.show()
                binding.appbar.fadeOutAndMoveY()

                with(binding.recyclerView) {
                    run {
                        val params = this.layoutParams as ViewGroup.MarginLayoutParams
                        params.bottomMargin = dp(56f) * -1
                        this.layoutParams = params
                    }
                }
            }

            scrollToBottom = true
        })

        AppInit.appTheme.observe(viewLifecycleOwner, Observer {
            val menuItem = binding.bottomAppBar.menu.findItem(R.id.theme)
            when (it) {
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    menuItem.setTitle(R.string.dark)
                    menuItem.setIcon(R.drawable.ic_dark)
                }
                AppCompatDelegate.MODE_NIGHT_NO -> {
                    menuItem.setTitle(R.string.light)
                    menuItem.setIcon(R.drawable.ic_light)
                }
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                    menuItem.setTitle(R.string.auto)
                    menuItem.setIcon(R.drawable.ic_auto)
                }
                else -> {
                    menuItem.setTitle(R.string.auto)
                    menuItem.setIcon(R.drawable.ic_auto)
                }
            }
        })

        val notificationsMuted = AppInit.tinyData.getBoolean("NOTIFICATIONS_MUTED", false)

        val menuItem = binding.bottomAppBar.menu.findItem(R.id.notifications)

        if (notificationsMuted) {
            menuItem.setIcon(R.drawable.ic_notifications_off)
        } else {
            menuItem.setIcon(R.drawable.ic_notifications_on)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notifications -> {
                val notificationsMuted = AppInit.tinyData.getBoolean("NOTIFICATIONS_MUTED", false)
                if (notificationsMuted) {
                    AppInit.tinyData.saveBoolean("NOTIFICATIONS_MUTED", false)
                    item.setIcon(R.drawable.ic_notifications_on)
                    Toast.makeText(context, R.string.notifications_on, Toast.LENGTH_SHORT).show()
                } else {
                    AppInit.tinyData.saveBoolean("NOTIFICATIONS_MUTED", true)
                    item.setIcon(R.drawable.ic_notifications_off)
                    Toast.makeText(context, R.string.notifications_off, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.theme -> {
                AppInit.appTheme.value = when (AppCompatDelegate.getDefaultNightMode()) {
                    AppCompatDelegate.MODE_NIGHT_YES -> {
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.MODE_NIGHT_NO -> {
                        AppCompatDelegate.MODE_NIGHT_YES
                    }
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                        AppCompatDelegate.MODE_NIGHT_NO
                    }
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        }
        return true
    }

    override fun onItemClicked(trackingItem: TrackingItemDescription) {
        if (viewModel.query.value != null) {
            viewModel.query.value = null
        } else {
            viewModel.query.value = trackingItem
        }
    }

    override fun onOptionsClicked(trackingItem: TrackingItemDescription) {
        OptionsDialogFragment.newInstance(
            arrayListOf(
                R.string.edit_title,
                if (trackingItem.status == TRANSIT) R.string.mark_as_delivered else R.string.mark_in_transit,
                R.string.copy_tracking_number,
                R.string.delete_this_tracking_number
            ),
            object : OptionsDialogFragment.ClickListener {
                override fun onOptionSelected(position: Int) {
                    when (position) {
                        0 -> EditTitleDialogFragment.newInstance(trackingItem)
                            .show(childFragmentManager, TAG)
                        1 -> ChangeStatus(trackingItem.trackingId)
                            .execute(if (trackingItem.status == DELIVERED) TRANSIT else DELIVERED)
                        2 -> {
                            clipboard.setPrimaryClip(ClipData.newPlainText("text", trackingItem.trackingId))
                            Toast.makeText(context, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show()
                        }
                        3 -> {
                            if (viewModel.query.value != null) {
                                viewModel.query.value = null
                            }
                            DeleteTrackingItem().execute(trackingItem.trackingId)
                        }
                    }
                }
            }).show(childFragmentManager, TAG)
    }
}

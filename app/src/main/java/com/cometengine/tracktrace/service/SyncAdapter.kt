package com.cometengine.tracktrace.service

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.cometengine.tracktrace.misc.enqueueWorker
import com.cometengine.tracktrace.workers.TrackingWorker

class SyncAdapter(context: Context) : AbstractThreadedSyncAdapter(context, true, false) {

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
        try {
            enqueueWorker<TrackingWorker>()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
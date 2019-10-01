package com.cometengine.tracktrace.database

import android.os.AsyncTask

abstract class RoomAsyncTask<T, R> : AsyncTask<T, Void, R>() {

    override fun doInBackground(vararg params: T): R {
        return doInBackground(params[0])
    }

    abstract fun doInBackground(param: T): R
}
package com.cometengine.tracktrace

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.cometengine.tracktrace.misc.ACCOUNT_TYPE
import com.cometengine.tracktrace.misc.AUTHORITY
import com.cometengine.tracktrace.fragments.MainFragment
import com.cometengine.tracktrace.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()

            try {
                checkAccount()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkAccount() {
        val accountManager = AccountManager.get(this)
        val account = Account(getString(R.string.app_name), ACCOUNT_TYPE)
        account.also {

            val args = Bundle()
            args.putString(AccountManager.KEY_ACCOUNT_NAME, getString(R.string.app_name))
            args.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE)

            accountManager.addAccountExplicitly(it, "", args)

            ContentResolver.setSyncAutomatically(it, AUTHORITY, true)
            ContentResolver.setIsSyncable(it, AUTHORITY, 1)
            ContentResolver.addPeriodicSync(it, AUTHORITY, args, 3600)
        }
    }

    override fun onBackPressed() {
        if (viewModel.query.value != null) {
            viewModel.query.value = null
        } else {
            super.onBackPressed()
        }
    }
}
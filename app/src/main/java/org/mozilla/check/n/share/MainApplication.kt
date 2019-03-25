package org.mozilla.check.n.share

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import io.fabric.sdk.android.Fabric
import okhttp3.OkHttpClient
import org.mozilla.check.n.share.entry.ClipService
import org.mozilla.check.n.share.persistence.ShareDatabase
import com.crashlytics.android.Crashlytics


class MainApplication : Application() {

    lateinit var database: ShareDatabase

    lateinit var apolloClient: ApolloClient
    private val endpoint: String = "https://cofacts-api.g0v.tw/graphql"

    override fun onCreate() {
        super.onCreate()

        initCrashlytics()

        initApolloClient()

        initRoom()

        initClipboardService()
    }

    private fun initCrashlytics() {
        if (!BuildConfig.DEBUG) {
            //  Collect report only when not debug
            Fabric.with(this, Crashlytics())
        }
    }

    private fun initClipboardService() {
        val intent = Intent()
        intent.component = ComponentName(this, ClipService::class.java)
        startService(intent)
    }

    private fun initRoom() {
        database = Room.databaseBuilder(
            this,
            ShareDatabase::class.java, "share.sqlite3"
        )
            .build()
    }

    private fun initApolloClient() {
        val okHttpClient = OkHttpClient.Builder()
            .build()

        apolloClient = ApolloClient.builder()
            .serverUrl(endpoint)
            .okHttpClient(okHttpClient)
            .build()
    }

}
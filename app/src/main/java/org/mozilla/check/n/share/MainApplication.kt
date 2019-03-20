package org.mozilla.check.n.share

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient
import org.mozilla.check.n.share.entry.ClipService
import org.mozilla.check.n.share.persistence.ShareDatabase

class MainApplication : Application() {

    lateinit var database: ShareDatabase

    lateinit var apolloClient: ApolloClient
    val endpoint: String = "https://cofacts-api.g0v.tw/graphql"

    override fun onCreate() {
        super.onCreate()

        val okHttpClient = OkHttpClient.Builder()
            .build()

        apolloClient = ApolloClient.builder()
            .serverUrl(endpoint)
            .okHttpClient(okHttpClient)
            .build()

        database = Room.databaseBuilder(this,
            ShareDatabase::class.java, "share.sqlite3")
            .build()

        val intent = Intent()
        intent.component = ComponentName(this, ClipService::class.java)
        startService(intent)
    }

}
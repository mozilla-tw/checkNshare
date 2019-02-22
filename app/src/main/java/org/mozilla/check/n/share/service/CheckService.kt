package org.mozilla.check.n.share.service

import android.app.Application
import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mozilla.check.n.share.MainApplication
import org.mozilla.check.n.share.persistence.ListArticlesQuery
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.persistence.type.*

class CheckService : IntentService(CheckService::class.java.simpleName) {

    val uiHandler: Handler = Handler(Looper.getMainLooper())

    override fun onHandleIntent(intent: Intent?) {

        val queryText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return

        val query = ListArticlesQuery.builder()
            .filter(
                ListArticleFilter.builder()
                    .moreLikeThis(
                        ListArticleMoreLikeThisInput.builder()
                            .like(queryText)
                            .build()
                    ).build()
            )
            .orderBy(
                listOf(
                    ListArticleOrderBy.builder()
                        ._score(SortOrderEnum.DESC)
                        .build()
                )
            )
            .build()

        val apolloClient = (application as MainApplication).apolloClient
        apolloClient
            .query(query)
            .enqueue(ApolloCallback<ListArticlesQuery.Data>(QueryCallback(application, queryText), uiHandler))
    }

    class QueryCallback(val application: Application, val queryText: String) :
        ApolloCall.Callback<ListArticlesQuery.Data>() {


        override fun onResponse(response: Response<ListArticlesQuery.Data>) {
            //  parse data
            val list = response.data()
            val edges = list?.connections()?.edges()
            var totalCount: Int = 0
            var rumorCount: Int = 0
            edges?.forEach {
                val articlesReplies = it.node()?.articleReplies()
                if (articlesReplies != null) {
                    totalCount += articlesReplies.size
                }
                articlesReplies?.forEach {
                    if (it.reply()?.type() == ReplyTypeEnum.RUMOR) {
                        rumorCount++
                    }
                }
            }

            val percent = if (edges == null || edges.isEmpty()) {
                0
            } else {
                rumorCount * 100 / totalCount
            }


            GlobalScope.launch {
                (application as MainApplication).database.shareDao().addShare(ShareEntity(queryText, percent))
            }

            val toastContent = if (rumorCount == 100) {
                "你轉貼的內容很可能是謠言喲！"
            } else if (rumorCount == 0) {
                "你轉貼的內容找不到相關的謠言".format(percent)
            } else {
                "你轉貼的內容有 %2d%% 的機率是謠言喲！".format(percent)
            }

            Toast.makeText(application, toastContent, Toast.LENGTH_LONG).show()
        }

        override fun onFailure(e: ApolloException) {
            Log.e("CheckNShare", e.message, e)
        }

    }

}
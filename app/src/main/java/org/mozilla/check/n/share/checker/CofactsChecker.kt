package org.mozilla.check.n.share.checker

import android.os.Handler
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.check.n.share.persistence.ListArticlesQuery
import org.mozilla.check.n.share.persistence.ShareEntity
import org.mozilla.check.n.share.persistence.type.*

class CofactsChecker(val apolloClient: ApolloClient) {

    interface CheckCallback {
        fun onCheckResult(shareEntity: ShareEntity)
        fun provideHandler(): Handler
    }

    fun check(shareEntity: ShareEntity, checkCallback: CheckCallback) {

        val queryText = shareEntity.contentText
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


        apolloClient
                .query(query)
                .enqueue(ApolloCallback<ListArticlesQuery.Data>(QueryCallback(checkCallback, shareEntity), checkCallback.provideHandler()))
    }

    class QueryCallback(val checkCallback: CheckCallback, val shareEntity: ShareEntity) :
            ApolloCall.Callback<ListArticlesQuery.Data>() {


        override fun onResponse(response: Response<ListArticlesQuery.Data>) {
            //  parse data
            val list = response.data()
            val edges = list?.connections()?.edges()
            var totalCount: Int = 0
            var rumorCount: Int = 0
            val json = JSONArray()
            edges?.forEach {
                val articlesReplies = it.node()?.articleReplies()
                if (articlesReplies != null) {
                    totalCount += articlesReplies.size
                }
                articlesReplies?.forEach {
                    val jsonObject = JSONObject()
                    if (it.reply()?.type() == ReplyTypeEnum.RUMOR) {
                        rumorCount++
                    }
                    jsonObject.put("TYPE", it.reply()?.type())
                    jsonObject.put("TEXT", it.reply()?.text())
                    json.put(jsonObject)
                }
            }

            val cofactsResponse = if (edges == null || edges.isEmpty()) {
                ShareEntity.RESPONSE_NEUTRAL
            } else if (rumorCount == 0){
                ShareEntity.RESPONSE_TRUE
            } else {
                ShareEntity.RESPONSE_FALSE
            }

            shareEntity.cofactsResponse = cofactsResponse
            shareEntity.cofactsExplanation = json.toString()


            checkCallback.onCheckResult(shareEntity)

        }

        override fun onFailure(e: ApolloException) {
            Log.e("CheckNShare", e.message, e)
        }

    }
}

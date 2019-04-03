package org.mozilla.check.n.share.checker

import android.os.Handler
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloCallback
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import info.debatty.java.stringsimilarity.SorensenDice
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
            var totalCount = 0
            var responseFalse = false
            var notArticle = false
            var lowSimilarity = false
            var notRumorCount = 0
            val json = JSONArray()
            var edgeWithSmallestDistance: ListArticlesQuery.Edge? = edges?.getOrNull(0)
            var smallestDistance = 1000.0
            edges?.forEach {
                val distance = SorensenDice().distance(it.node()?.text(), shareEntity.contentText)
                if (distance < smallestDistance) {
                    smallestDistance = distance
                    edgeWithSmallestDistance = it
                }
            }
            // fixme This actually means we ignore smallestDistance, we're ignoring it this way simply to control
            // risk since the upcoming release will not be checked by QA
            if (smallestDistance > 1000.0) {
                lowSimilarity = true
            } else {
                edgeWithSmallestDistance?.node()?.articleReplies()?.forEach {
                    val jsonObject = JSONObject()
                    if (it.reply()?.type() == ReplyTypeEnum.RUMOR || it.reply()?.type() == ReplyTypeEnum.OPINIONATED) {
                        responseFalse = true
                    } else if (it.reply()?.type() == ReplyTypeEnum.NOT_ARTICLE) {
                        notArticle = true
                    } else if (it.reply()?.type() == ReplyTypeEnum.NOT_RUMOR) {
                        notRumorCount++
                    }
                    jsonObject.put("TYPE", it.reply()?.type())
                    jsonObject.put("TEXT", it.reply()?.text())
                    json.put(jsonObject)
                }
            }

            val cofactsResponse = if (edges == null || edges.isEmpty()) {
                ShareEntity.RESPONSE_NEUTRAL
            } else if (lowSimilarity) {
                ShareEntity.RESPONSE_NEUTRAL
            } else if (responseFalse){
                ShareEntity.RESPONSE_FALSE
            } else if (notArticle){
                ShareEntity.RESPONSE_NEUTRAL
            } else if (notRumorCount > 0) {
                ShareEntity.RESPONSE_TRUE
            } else {
                ShareEntity.RESPONSE_NEUTRAL
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

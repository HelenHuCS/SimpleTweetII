package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException

private const val TAG = "TimelineActivity"
const val REQUEST_CODE = 20
class TimelineActivity : AppCompatActivity() {

    lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var scrollListener: EndlessRecyclerViewScrollListener
    lateinit var client:TwitterClient
    lateinit var rvTweets:RecyclerView
    lateinit var adapter: TweetAdapter
    lateinit var toolbar: Toolbar

    val tweets = ArrayList<Tweet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Rerfreshing timeline")
            scrollListener.resetState()
            populateHomeTimeLine()
        }

        swipeContainer.setColorSchemeColors(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetAdapter(this,tweets)

        val linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        rvTweets.adapter = adapter
        rvTweets.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager){
            override fun onLoadMore(page: Int, totalItemCount: Int, view: RecyclerView) {
                populateHomeTimeLineNext(getLastVisibleItemId(tweets))
            }
        }
        rvTweets.addOnScrollListener(scrollListener)

        populateHomeTimeLine()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setTitle(getString(R.string.app_name))
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
//        return super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.compose) {
            val intent = Intent(this,ComposeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            val tweet = data?.getParcelableExtra<Tweet>("tweet")
            if (tweet != null) {
                tweets.add(0,tweet)
                adapter.notifyItemInserted(0)
                rvTweets.smoothScrollToPosition(0)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun populateHomeTimeLineNext(since_id:Long) {
        client.getHomeTimeLineSince(since_id,object : JsonHttpResponseHandler(){
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.d(TAG, "onFailure: ")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val jsonArray = json.jsonArray
                    val tweetsTmp = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(tweets.size,tweetsTmp)
                    adapter.notifyDataSetChanged()
                } catch (e:JSONException) {
                    Log.e(TAG, "onSuccess: ",e )
                }
            }
        })
    }

    fun populateHomeTimeLine() {
        client.getHomeTimeLine(object : JsonHttpResponseHandler(){
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.d(TAG, "onFailure: $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.d(TAG, "onSuccess: success: $json")
                try {
                    adapter.clear()
                    val jsonArray = json.jsonArray
                    val tweetsTmp = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(tweetsTmp)
                    adapter.notifyDataSetChanged()
                    swipeContainer.isRefreshing = false
                } catch (e:JSONException) {
                    Log.e(TAG, "onSuccess: ",e )
                }
            }

        })
    }
}
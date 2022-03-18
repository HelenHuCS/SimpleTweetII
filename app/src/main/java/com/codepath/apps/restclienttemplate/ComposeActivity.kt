package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

private const val TAG = "ComposeActivity"
class ComposeActivity : AppCompatActivity() {

    lateinit var etCompose:EditText
    lateinit var btnTweet:Button

    lateinit var client: TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)

        client = TwitterApplication.getRestClient(this)

        btnTweet.setOnClickListener {
            val tweetContent = etCompose.text.toString()
            if (tweetContent.isEmpty()) {
                Toast.makeText(this,"Empty tweets not allowed !",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tweetContent.length > 280 ) {
                Toast.makeText(this,"Tweet is too long ! limit is 280 characters .",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            client.publishTweet(tweetContent,object : JsonHttpResponseHandler() {
                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.d(TAG, "onFailure: ",throwable)
                }

                override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                    Log.d(TAG, "onSuccess: successfully published tweet !")

                    val tweet = Tweet.fromJson(json.jsonObject)
                    val intent = Intent()
                    intent.putExtra("tweet",tweet)
                    setResult(RESULT_OK,intent)
                    finish()
                }

            })
        }
    }
}
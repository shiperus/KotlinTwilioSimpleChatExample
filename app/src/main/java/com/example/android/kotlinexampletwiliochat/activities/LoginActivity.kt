package com.example.android.kotlinexampletwiliochat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.android.kotlinexampletwiliochat.AppChatClient
import com.example.android.kotlinexampletwiliochat.KotlinExampleTwilioChatApplication
import com.example.android.kotlinexampletwiliochat.R

class LoginActivity : BaseActivity(), AppChatClient.ChatClientCompletionListener {
    private lateinit var editTextUsername: EditText
    private lateinit var buttonLogin: Button
    private lateinit var tokenTwilio: String
    private  var userIdentity: String=""
    private lateinit var appChatClient: AppChatClient
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as KotlinExampleTwilioChatApplication)
                .appChatClient.chatClientCompletionListener = this
        checkUserAlreadyLogin()
        setContentView(R.layout.activity_login)
        editTextUsername = findViewById(R.id.et_username)
        buttonLogin = findViewById(R.id.btn_login)
        buttonLogin.setOnClickListener({
            userIdentity = editTextUsername.text.toString()
            if (userIdentity.isEmpty())
                return@setOnClickListener
            showLoading()
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            (application as KotlinExampleTwilioChatApplication).apiHelper.getTwilioToken(
                    Volley.newRequestQueue(this),
                    deviceId,
                    userIdentity,
                    Response.Listener { response ->
                        if (isFinishing)
                            return@Listener
                        tokenTwilio = response.getString("token")
                        sharedPrefs.edit().putString("token_twilio", tokenTwilio).apply()
                        (application as KotlinExampleTwilioChatApplication)
                                .appChatClient.createChatClient(
                                tokenTwilio
                        )
                    },
                    Response.ErrorListener { error ->
                        hideLoading()
                        Toast.makeText(
                                this,
                                "There was an error, please try again",
                                Toast.LENGTH_LONG).show()
                    })
        })
    }

    private fun checkUserAlreadyLogin() {
        if (!sharedPrefs.getString("username", "").isEmpty()) {
            directToChannelListActivity()
        }
    }

    override fun onChatClientInitSuccess() {
        hideLoading()
        if(!userIdentity.isEmpty())
            sharedPrefs.edit().putString("username", userIdentity).apply()
        directToChannelListActivity()
    }

    fun directToChannelListActivity() {
        val intentToListUser = Intent(this, ChannelListActivity::class.java)
        startActivity(intentToListUser)
        finish()
    }

    override fun onChatClientInitError(stringError: String?) {
        hideLoading()
        if (null != stringError)
            Toast.makeText(this, stringError, Toast.LENGTH_LONG).show()
    }
}

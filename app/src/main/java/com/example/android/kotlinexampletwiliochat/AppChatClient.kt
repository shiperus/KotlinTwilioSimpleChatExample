package com.example.android.kotlinexampletwiliochat

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.android.kotlinexampletwiliochat.api.ApiHelper
import com.twilio.accessmanager.AccessManager
import com.twilio.chat.CallbackListener
import com.twilio.chat.ChatClient
import com.twilio.chat.ErrorInfo

class AppChatClient(private var context: Context) : CallbackListener<ChatClient>(),
        AccessManager.TokenUpdateListener,
        AccessManager.Listener {

    var chatClientCompletionListener: ChatClientCompletionListener? = null
    var accessManager: AccessManager? = null
    private lateinit var token: String
    var chatClient: ChatClient? = null
        private set

    interface ChatClientCompletionListener {
        fun onChatClientInitSuccess()
        fun onChatClientInitError(stringError: String?)
    }

    fun createChatClient(
            token: String
    ) {
        this.token = token
        val props = ChatClient.Properties.Builder().createProperties()
        ChatClient.create(
                context,
                token,
                props,
                this
        )
    }

    private fun createAccessManager(token: String) {
        if (null == accessManager) {
            accessManager = AccessManager(token, this)
            accessManager?.addTokenUpdateListener(this)
        } else {
            accessManager?.updateToken(token)
        }
    }

    fun shutdown() {
        chatClient!!.shutdown()
        chatClient = null
        accessManager?.removeTokenUpdateListener(this)
        accessManager = null
    }

    @SuppressLint("HardwareIds")
    private fun getToken() {
        val deviceId = Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID)
        val apiHelper = ApiHelper()

        apiHelper.getTwilioToken(
                Volley.newRequestQueue(context),
                deviceId,
                chatClient!!.myIdentity,
                Response.Listener { response ->
                    val tokenTwilio = response.getString("token")
                    accessManager?.updateToken(tokenTwilio)
                },
                Response.ErrorListener { error ->
                    if (null != chatClientCompletionListener) {
                        chatClientCompletionListener?.onChatClientInitError(error.message)
                    }
                })
    }

    override fun onSuccess(chatClient: ChatClient?) {
        if (null != chatClient)
            this.chatClient = chatClient
        if (null != chatClientCompletionListener)
            chatClientCompletionListener?.onChatClientInitSuccess()
        createAccessManager(token)
    }

    override fun onError(errorInfo: ErrorInfo?) {
        if (null != chatClientCompletionListener)
            chatClientCompletionListener?.onChatClientInitError(errorInfo?.message)
    }

    override fun onTokenUpdated(token: String?) {
        if (null != token) {
            if (null != chatClient)
                chatClient!!.updateToken(token, null)
            else
                createChatClient(token)
        }
    }

    override fun onError(p0: AccessManager?, p1: String?) {
    }

    override fun onTokenExpired(p0: AccessManager?) {
        if(null == chatClient)
            return
        getToken()
    }

    override fun onTokenWillExpire(p0: AccessManager?) {
        if(null == chatClient)
            return
        getToken()
    }


}
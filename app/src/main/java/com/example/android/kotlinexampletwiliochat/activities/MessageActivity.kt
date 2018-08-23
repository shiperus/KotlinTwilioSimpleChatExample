package com.example.android.kotlinexampletwiliochat.activities

import android.annotation.SuppressLint
import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.android.kotlinexampletwiliochat.AppChatClient
import com.example.android.kotlinexampletwiliochat.KotlinExampleTwilioChatApplication
import com.example.android.kotlinexampletwiliochat.R
import com.example.android.kotlinexampletwiliochat.adapter.MessagesRecyclerViewAdapter
import com.example.android.kotlinexampletwiliochat.receiver.NetworkStateChangedReceiver
import com.twilio.chat.*
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : BaseActivity(),
        NetworkStateChangedReceiver.NetworkStateListener,
        AppChatClient.ChatClientCompletionListener,
        ChannelListener {
    private var channel: Channel? = null
    private var mChatClient: ChatClient? = null
    private lateinit var networkStateChangedReceiver: NetworkStateChangedReceiver
    lateinit var intentFilter: IntentFilter
    private lateinit var linearLayoutOfflineContainer: LinearLayout
    private lateinit var linearLayoutReconnectingContainer: LinearLayout

    lateinit var recyclerViewMessages: RecyclerView
    lateinit var editTextSendMessage: EditText
    lateinit var buttonSendMessage: Button
    var arrayListMessages = ArrayList<Message>()
    lateinit var adapter:MessagesRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        linearLayoutOfflineContainer = findViewById(R.id.ly_offline_container)
        linearLayoutOfflineContainer.visibility = View.GONE
        linearLayoutReconnectingContainer = findViewById(R.id.ly_reconnecting_container)
        linearLayoutReconnectingContainer.visibility = View.GONE

        recyclerViewMessages = findViewById(R.id.rv_messages)
        editTextSendMessage = findViewById(R.id.et_send_message)
        buttonSendMessage = findViewById(R.id.btn_send_message)
        buttonSendMessage.isEnabled = false
        (application as KotlinExampleTwilioChatApplication).appChatClient
                .chatClientCompletionListener = this
        intentFilter = IntentFilter()
        networkStateChangedReceiver = NetworkStateChangedReceiver()
        networkStateChangedReceiver.networkStateListener = this
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        adapter = MessagesRecyclerViewAdapter(this)
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.adapter = adapter
    }

    override fun onPause() {
        unregisterReceiver(networkStateChangedReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (null != intent) {
            channel = intent.getParcelableExtra("CHANNEL")
            registerReceiver(networkStateChangedReceiver, intentFilter)
            mChatClient = (application as KotlinExampleTwilioChatApplication).appChatClient.chatClient
            showLoading()
            if (null == mChatClient) {
                createChatClient()
            } else {
                if ((application as KotlinExampleTwilioChatApplication).appChatClient.accessManager?.isTokenExpired!!) {
                    createChatClient()
                } else {
                    initUI()
                }
            }
        }
    }

    private fun initUI() {
        val channelObject = mChatClient?.channels
        val channelSid = intent.getStringExtra("CHANNEL_SID")
        channelObject?.getChannel(channelSid, object : CallbackListener<Channel>() {
            override fun onSuccess(channel: Channel) {
                hideLoading()
                val currentUserIdentity = mChatClient?.myIdentity.toString()
                var channelName = channel?.friendlyName.toString()
                val channelNameSplit = channelName.split('#')
                for (channelNameSplitItem in channelNameSplit) {
                    if (channelNameSplitItem != currentUserIdentity) {
                        channelName = channelNameSplitItem
                        break
                    }
                }
                title = channelName
                this@MessageActivity.channel = channel
                channel.addListener(this@MessageActivity)

                loadMessages(false)


            }

            override fun onError(errorInfo: ErrorInfo?) {
                hideLoading()
                Toast.makeText(this@MessageActivity, errorInfo?.message, Toast.LENGTH_LONG).show()
            }
        })

        initSendMessage()
    }

    private fun initSendMessage() {
        editTextSendMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(content: CharSequence?, p1: Int, p2: Int, p3: Int) {
                buttonSendMessage.isEnabled = !content.toString().isEmpty()
            }

        })

        buttonSendMessage.setOnClickListener({
            showLoading()
            channel?.messages?.sendMessage(
                    Message.options().withBody(et_send_message.text.toString()),
                    object : CallbackListener<Message>() {
                        override fun onSuccess(p0: Message?) {
                            hideLoading()
                            loadMessages(true)
                        }

                        override fun onError(errorInfo: ErrorInfo?) {
                            hideLoading()
                        }
                    }
            )
        })
    }

    private fun loadMessages(isSendMessage: Boolean) {
        channel?.messages?.getLastMessages(50, object : CallbackListener<List<Message>>() {
            override fun onSuccess(listMessage: List<Message>?) {
                if (listMessage != null) {
                    arrayListMessages.clear()
                    for (msg in listMessage) {
                        arrayListMessages.add(msg)
                    }
                    adapter.arrayListMessages = arrayListMessages
                    adapter.currentUserIdentity = mChatClient?.myIdentity.toString()
                    adapter.notifyDataSetChanged()
                    if(isSendMessage)
                        recyclerViewMessages.smoothScrollToPosition(arrayListMessages.size-1)
                }
            }

        })
    }

    @SuppressLint("HardwareIds")
    private fun createChatClient() {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        (application as KotlinExampleTwilioChatApplication).apiHelper.getTwilioToken(
                Volley.newRequestQueue(this),
                deviceId,
                sharedPrefs.getString("username", ""),
                Response.Listener { response ->
                    if (isFinishing)
                        return@Listener
                    val tokenTwilio = response.getString("token")
                    (application as KotlinExampleTwilioChatApplication)
                            .appChatClient.createChatClient(
                            tokenTwilio
                    )
                },
                Response.ErrorListener { error ->
                    hideLoading()
                    error.stackTrace
                    Toast.makeText(
                            this,
                            "There was an error, please try again",
                            Toast.LENGTH_LONG
                    ).show()
                })
    }

    override fun networkAvailable() {
        if (isFinishing)
            return
        if (linearLayoutOfflineContainer.visibility == View.VISIBLE) {
            linearLayoutOfflineContainer.visibility = View.GONE
            linearLayoutReconnectingContainer.visibility = View.VISIBLE
            createChatClient()
        }
    }

    override fun networkUnavailable() {
        if (isFinishing)
            return
        linearLayoutOfflineContainer.visibility = View.VISIBLE
        linearLayoutReconnectingContainer.visibility = View.GONE

    }

    override fun onChatClientInitSuccess() {
        mChatClient = (application as KotlinExampleTwilioChatApplication).appChatClient.chatClient
        linearLayoutOfflineContainer.visibility = View.GONE
        linearLayoutReconnectingContainer.visibility = View.GONE
        initUI()
    }

    override fun onChatClientInitError(stringError: String?) {
        if (null != stringError)
        hideLoading()
    }

    override fun onMemberDeleted(p0: Member?) {
    }

    override fun onTypingEnded(p0: Channel?, p1: Member?) {
    }

    override fun onMessageAdded(p0: Message?) {
        loadMessages(false)
    }

    override fun onMessageDeleted(p0: Message?) {
    }

    override fun onMemberAdded(p0: Member?) {
    }

    override fun onTypingStarted(p0: Channel?, p1: Member?) {
    }

    override fun onSynchronizationChanged(p0: Channel?) {
    }

    override fun onMessageUpdated(p0: Message?, p1: Message.UpdateReason?) {
    }

    override fun onMemberUpdated(p0: Member?, p1: Member.UpdateReason?) {
    }
}

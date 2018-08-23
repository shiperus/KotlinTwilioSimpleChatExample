package com.example.android.kotlinexampletwiliochat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.android.kotlinexampletwiliochat.AppChatClient
import com.example.android.kotlinexampletwiliochat.KotlinExampleTwilioChatApplication
import com.example.android.kotlinexampletwiliochat.R
import com.example.android.kotlinexampletwiliochat.model.ChannelModel
import com.example.android.kotlinexampletwiliochat.receiver.NetworkStateChangedReceiver
import com.twilio.chat.*


class ChannelListActivity : BaseActivity(),
        AppChatClient.ChatClientCompletionListener,
        ChatClientListener,
        NetworkStateChangedReceiver.NetworkStateListener {
    private var mChatClient: ChatClient? = null
    private lateinit var linearLayoutChatList: LinearLayout
    private lateinit var linearLayoutOfflineContainer: LinearLayout
    private lateinit var linearLayoutReconnectingContainer: LinearLayout

    private lateinit var textViewUserIdentity: TextView
    private lateinit var networkStateChangedReceiver: NetworkStateChangedReceiver
    lateinit var intentFilter: IntentFilter
    lateinit var selectedSidChannel: String
    val hashMapChannel: HashMap<String, ChannelModel> = HashMap()

//    lateinit var buttonCreateChannel: Button
//    lateinit var editTextChannelName: EditText

    lateinit var buttonChatUser: Button
    lateinit var editTextChatUser: EditText

    lateinit var buttonLogout: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chat_list)

        linearLayoutOfflineContainer = findViewById(R.id.ly_offline_container)
        linearLayoutOfflineContainer.visibility = View.GONE
        linearLayoutReconnectingContainer = findViewById(R.id.ly_reconnecting_container)
        linearLayoutReconnectingContainer.visibility = View.GONE

//        buttonCreateChannel = findViewById(R.id.btn_create_channel)
//        editTextChannelName = findViewById(R.id.et_channel_name)

        buttonChatUser = findViewById(R.id.btn_chat_user)
        editTextChatUser = findViewById(R.id.et_user_identity)

        buttonLogout = findViewById(R.id.btn_logout)



//        buttonCreateChannel.setOnClickListener({
//            mChatClient?.channels?.createChannel(
//                    editTextChannelName.text.toString(),
//                    Channel.ChannelType.PUBLIC,
//                    object : CallbackListener<Channel>() {
//                        override fun onSuccess(channel: Channel) {
//                            Toast.makeText(
//                                    this@ChannelListActivity,
//                                    "Success Create Channel",
//                                    Toast.LENGTH_LONG
//                            ).show()
//                            refreshChannelList()
//                        }
//
//                        override fun onError(errorInfo: ErrorInfo?) {
//                            Toast.makeText(
//                                    this@ChannelListActivity,
//                                    "Failed Create Channel",
//                                    Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    })
//        })

        buttonChatUser.setOnClickListener({
            showLoading()
            if (isUserAlreadyAddedToPrivateChat(editTextChatUser.text.toString())) {
                Toast.makeText(this, "User already added to chat", Toast.LENGTH_LONG).show()
                hideLoading()
            } else {
                mChatClient?.channels?.createChannel(
                        editTextChatUser.text.toString() +"#"+ mChatClient?.myIdentity,
                        Channel.ChannelType.PRIVATE,
                        object : CallbackListener<Channel>() {
                            override fun onSuccess(channel: Channel) {
                                val channelCreated = channel
                                channelCreated.members.addByIdentity(mChatClient?.myIdentity, object : StatusListener() {
                                    override fun onSuccess() {

                                    }
                                })

                                channelCreated.members.addByIdentity(editTextChatUser.text.toString(), object : StatusListener() {
                                    override fun onSuccess() {
                                        hideLoading()
                                        Toast.makeText(
                                                this@ChannelListActivity,
                                                "Success Create Channel",
                                                Toast.LENGTH_LONG
                                        ).show()
                                        refreshChannelList()
                                    }
                                })
                            }

                            override fun onError(errorInfo: ErrorInfo?) {
                                Toast.makeText(
                                        this@ChannelListActivity,
                                        "Failed Create Channel",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        })
            }
        })

        buttonLogout.setOnClickListener({
            sharedPrefs.edit().clear().apply()
            (application as KotlinExampleTwilioChatApplication).appChatClient.shutdown()
            directToLoginActivity()
        })

        (application as KotlinExampleTwilioChatApplication).appChatClient
                .chatClientCompletionListener = this
        intentFilter = IntentFilter()
        networkStateChangedReceiver = NetworkStateChangedReceiver()
        networkStateChangedReceiver.networkStateListener = this
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    private fun directToLoginActivity() {
        val intentLoginActivity = Intent(this, LoginActivity::class.java)
        startActivity(intentLoginActivity)
        finish()
    }

    private fun isUserAlreadyAddedToPrivateChat(userChatName: String): Boolean {
        for (ch in mChatClient?.channels?.subscribedChannels!!) {
            if (null != ch.members.getMember(userChatName))
                return true
        }
        return false
    }

    override fun onPause() {
        unregisterReceiver(networkStateChangedReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(networkStateChangedReceiver, intentFilter)
        mChatClient = (application as KotlinExampleTwilioChatApplication).appChatClient.chatClient
        if (null == mChatClient) {
            showLoading()
            createChatClient()
        } else {
            if ((application as KotlinExampleTwilioChatApplication).appChatClient.accessManager?.isTokenExpired!!) {
                showLoading()
                createChatClient()
            } else {
                initLayout()
                loadUserChannels()
            }
        }
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

    private fun initLayout() {
        mChatClient?.setListener(this)
        linearLayoutChatList = findViewById(R.id.ly_chat_list)
        textViewUserIdentity = findViewById(R.id.tv_user_identity)
        textViewUserIdentity.text = "Welcome ${mChatClient?.myIdentity}"
    }

    private fun refreshChannelList() {
        linearLayoutChatList.removeAllViews()
        val subscribedChannels = mChatClient?.channels?.subscribedChannels
        if (subscribedChannels != null) {
            for (channel in subscribedChannels) {
                hashMapChannel[channel.sid] = ChannelModel(channel)
            }
        }
        for (key in hashMapChannel.keys) {
            val channel = hashMapChannel[key]
            val view: View = layoutInflater.inflate(R.layout.layout_channel_item, null)
            val textViewChannel: TextView = view.findViewById(R.id.tv_channel_name)
            val currentUserIdentity = mChatClient?.myIdentity.toString()
            var channelName = channel?.friendlyName.toString()
            val channelNameSplit = channelName.split('#')
            for(channelNameSplitItem in channelNameSplit){
                if(channelNameSplitItem != currentUserIdentity){
                    channelName = channelNameSplitItem
                    break
                }
            }
            textViewChannel.text = channelName
            view.setOnClickListener({
                selectedSidChannel = key
                val ch = hashMapChannel[selectedSidChannel]
//                ch?.destroy(object : StatusListener(){
//                    override fun onSuccess() {
//                        Toast.makeText(
//                                    this@ChannelListActivity,
//                                    "Success Delete", Toast.LENGTH_LONG
//                            ).show()
//                    }
//
//                })
                if (ch?.status == Channel.ChannelStatus.JOINED) {
                    directToMessageActivity(ch)
                } else if (ch?.status != Channel.ChannelStatus.JOINED) {
                    showLoading()
                    ch?.join(object : StatusListener() {
                        override fun onSuccess() {
                            hideLoading()
                            Toast.makeText(
                                    this@ChannelListActivity,
                                    "Success Join", Toast.LENGTH_LONG
                            ).show()
                            refreshChannelList()
                        }

                        override fun onError(errorInfo: ErrorInfo?) {
                            hideLoading()
                            Toast.makeText(
                                    this@ChannelListActivity,
                                    "Error Join ${errorInfo?.message}", Toast.LENGTH_LONG
                            ).show()
                        }

                    })
                }
            })
            linearLayoutChatList.addView(view)
        }
    }

    private fun directToMessageActivity(channel: ChannelModel) {
        showLoading()
        channel.getChannelObject(object : CallbackListener<Channel>() {
            override fun onSuccess(ch: Channel) {
                hideLoading()
                val intentToMessageActivity = Intent(this@ChannelListActivity,
                        MessageActivity::class.java)
                intentToMessageActivity.putExtra("CHANNEL_SID", ch.sid)
                startActivity(intentToMessageActivity)
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Toast.makeText(this@ChannelListActivity, errorInfo?.message, Toast.LENGTH_LONG).show()
                hideLoading()
            }
        })
    }

    private fun loadUserChannels() {
        mChatClient?.channels?.getUserChannelsList(
                object : CallbackListener<Paginator<ChannelDescriptor>>() {
                    override fun onSuccess(channelData: Paginator<ChannelDescriptor>?) {
                        if (channelData != null) {
                            for (item in channelData.items) {
                                hashMapChannel[item.sid] = ChannelModel(item)
                            }
                            refreshChannelList()
                        }
                    }

                    override fun onError(errorInfo: ErrorInfo?) {
                        super.onError(errorInfo)
                    }
                })
//        mChatClient?.channels?.getPublicChannelsList(
//                object : CallbackListener<Paginator<ChannelDescriptor>>() {
//                    override fun onSuccess(channelData: Paginator<ChannelDescriptor>?) {
//                        if (channelData != null) {
//                            for (item in channelData.items) {
//
//                                hashMapChannel[item.sid] = ChannelModel(item)
//                            }
//                            refreshChannelList()
//                        }
//                    }
//
//                    override fun onError(errorInfo: ErrorInfo?) {
//                        super.onError(errorInfo)
//                    }
//                })
    }

    override fun onChatClientInitSuccess() {
        mChatClient = (application as KotlinExampleTwilioChatApplication).appChatClient.chatClient
        hideLoading()
        linearLayoutOfflineContainer.visibility = View.GONE
        linearLayoutReconnectingContainer.visibility = View.GONE
        initLayout()
        loadUserChannels()
    }

    override fun onChatClientInitError(stringError: String?) {
        if (null != stringError)
        hideLoading()
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


    //    ChatClientListener
    override fun onChannelDeleted(channel: Channel) {
        Handler().postDelayed({
            loadUserChannels()
        }, 1000)
    }

    override fun onInvitedToChannelNotification(p0: String?) {
    }

    override fun onClientSynchronization(p0: ChatClient.SynchronizationStatus?) {
    }

    override fun onNotificationSubscribed() {
    }

    override fun onUserSubscribed(p0: User?) {
    }

    override fun onChannelUpdated(channel: Channel, p1: Channel.UpdateReason?) {
        Handler().postDelayed({
            loadUserChannels()
        }, 1000)
    }

    override fun onRemovedFromChannelNotification(p0: String?) {
    }

    override fun onNotificationFailed(p0: ErrorInfo?) {
    }

    override fun onChannelJoined(channel: Channel) {
        Handler().postDelayed({
            loadUserChannels()
        }, 1000)
    }

    override fun onChannelAdded(channel: Channel) {
        Handler().postDelayed({
            loadUserChannels()
        }, 1000)
    }

    override fun onChannelSynchronizationChange(p0: Channel?) {
    }

    override fun onUserUnsubscribed(p0: User?) {
    }

    override fun onAddedToChannelNotification(p0: String?) {
    }

    override fun onChannelInvited(p0: Channel?) {
    }

    override fun onNewMessageNotification(p0: String?, p1: String?, p2: Long) {
    }

    override fun onConnectionStateChange(p0: ChatClient.ConnectionState?) {
    }

    override fun onError(p0: ErrorInfo?) {
    }

    override fun onUserUpdated(p0: User?, p1: User.UpdateReason?) {
    }
}

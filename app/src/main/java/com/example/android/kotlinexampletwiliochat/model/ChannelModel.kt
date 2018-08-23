package com.example.android.kotlinexampletwiliochat.model

import com.twilio.chat.*

class ChannelModel {
    private var channel: Channel? = null
    private var channelDescriptor: ChannelDescriptor? = null

    constructor(channel_: Channel) {
        channel = channel_

    }

    constructor(channel_: ChannelDescriptor) {
        channelDescriptor = channel_
    }

    val status:Channel.ChannelStatus
        get() {
            if(null!=channel) return channel!!.status
            if(null!=channelDescriptor) return channelDescriptor!!.status
            throw Exception("State Error")
        }

    val friendlyName: String
        get() {
            if(null!=channel) return channel!!.friendlyName
            if(null!=channelDescriptor) return channelDescriptor!!.friendlyName
            throw Exception("State Error")
        }

    fun getChannelObject(listener: CallbackListener<Channel>){
        if(null!=channel){
            listener.onSuccess(channel)
            return
        }
        if(null!=channelDescriptor){
            channelDescriptor!!.getChannel(object :CallbackListener<Channel>(){
                override fun onSuccess(channel: Channel) {
                    listener.onSuccess(channel)
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    listener.onError(errorInfo)
                }

            })
            return
        }
        listener.onError(ErrorInfo(-1,"No Channel In Model"))
    }

    fun join(listener:StatusListener){
        if(null!=channel){
            channel!!.join(listener)
            return
        }
        if(null!=channelDescriptor){
            channelDescriptor!!.getChannel(object :CallbackListener<Channel>(){
                override fun onSuccess(channel: Channel) {
                    channel.join(listener)
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    listener.onError(errorInfo)
                }

            })
            return
        }
        listener.onError(ErrorInfo(-1,"No Channel In Model"))
    }

    fun destroy(listener:StatusListener){
        if(null!=channel){
            channel!!.destroy(listener)
            return
        }
        if(null!=channelDescriptor){
            channelDescriptor!!.getChannel(object :CallbackListener<Channel>(){
                override fun onSuccess(channel: Channel) {
                    channel.destroy(listener)
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    listener.onError(errorInfo)
                }

            })
            return
        }
        listener.onError(ErrorInfo(-1,"No Channel In Model"))
    }
}
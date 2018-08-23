package com.example.android.kotlinexampletwiliochat.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.android.kotlinexampletwiliochat.R
import com.twilio.chat.Message

class MessagesRecyclerViewAdapter(var context: Context) : RecyclerView.Adapter<MessagesRecyclerViewAdapter.ViewHolder>() {

    var arrayListMessages = ArrayList<Message>()
    var currentUserIdentity = ""


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewMessageContentCurrentUser: TextView = view.findViewById(R.id.tv_message_content_current_user)
        var textViewMessageContentOtherUser: TextView = view.findViewById(R.id.tv_message_content_other_user)
        var linearLayoutContainerCurrentUser: LinearLayout = view.findViewById(R.id.ly_container_message_current_user)
        var linearLayoutContainerOtherUser: LinearLayout = view.findViewById(R.id.ly_container_message_other_user)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListMessages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (currentUserIdentity.equals(arrayListMessages[position].author) ) {
            holder.linearLayoutContainerOtherUser.visibility = View.GONE
            holder.linearLayoutContainerCurrentUser.visibility = View.VISIBLE
            holder.textViewMessageContentCurrentUser.text = arrayListMessages[position].messageBody
        } else {
            holder.linearLayoutContainerOtherUser.visibility = View.VISIBLE
            holder.linearLayoutContainerCurrentUser.visibility = View.GONE
            holder.textViewMessageContentOtherUser.text = arrayListMessages[position].messageBody
        }
    }
}
package com.example.android.kotlinexampletwiliochat

import android.content.Context
import android.widget.Toast
import com.twilio.chat.ErrorInfo
import com.twilio.chat.StatusListener

class ToastStatusListener(val context: Context,val successMsg:String,val errorMsg:String):
        StatusListener() {
    override fun onSuccess() {
        Toast.makeText(context,successMsg,Toast.LENGTH_LONG).show()
    }

    override fun onError(errorInfo: ErrorInfo?) {
        Toast.makeText(context,errorMsg,Toast.LENGTH_LONG).show()
    }



}
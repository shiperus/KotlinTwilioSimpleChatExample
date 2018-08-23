package com.example.android.kotlinexampletwiliochat.api

import android.provider.Settings
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.android.kotlinexampletwiliochat.BuildConfig
import com.example.android.kotlinexampletwiliochat.KotlinExampleTwilioChatApplication
import org.json.JSONObject

class ApiHelper {
    fun getTwilioToken(
            queue: RequestQueue,
            deviceId: String,
            userIdentity: String,
            successListener: Response.Listener<JSONObject>,
            errorListener: Response.ErrorListener
    ) {
        val mapRequest: HashMap<String, String> = HashMap()
        mapRequest.put("deviceId", deviceId)
        mapRequest.put("identity", userIdentity)
        val baseUrl = BuildConfig.BASE_URL
        val loginUrl = "$baseUrl/token.php"
        val obj = JSONObject(mapRequest)
        val jsonObjReq = JsonObjectRequest(
                Request.Method.POST,
                loginUrl,
                obj,
                successListener,
                errorListener
        )
        queue.add(jsonObjReq)
    }


}
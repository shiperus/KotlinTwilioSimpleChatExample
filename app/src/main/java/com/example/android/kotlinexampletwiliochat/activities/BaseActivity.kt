package com.example.android.kotlinexampletwiliochat.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    lateinit var sharedPrefs: SharedPreferences
        private set
    val PREFS_NAME = "prefs"
    protected lateinit var prog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prog = ProgressDialog(this)
        prog.setMessage("Loading...")
        prog.setCancelable(false)
//        appDatabase = Room.databaseBuilder(
//                applicationContext,
//                AppDatabase::class.java,
//                "dbExample")
//                .build()
    }

    protected fun showLoading() {
        prog.show()
    }

    protected fun hideLoading() {
        prog.hide()
    }



}
package com.example.android.kotlinexampletwiliochat

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class User {
    @ColumnInfo(name = "user_name")
    @PrimaryKey
    var userName:String=""

    @ColumnInfo(name = "user_token")
    @PrimaryKey
    var userToken:String=""
}
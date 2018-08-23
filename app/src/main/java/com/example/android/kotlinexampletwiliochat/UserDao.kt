package com.example.android.kotlinexampletwiliochat

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE user_name = :userName ")
    fun getExistingUser(userName: String):User

    @Insert
    fun insertUser(user: User)
}
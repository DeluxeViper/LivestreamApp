package com.deluxe_viper.livestreamapp.business.datasource.cache.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user_properties")
    suspend fun findAllUsers(): List<UserEntity>

    @Query("SELECT * FROM user_properties WHERE email = :email")
    suspend fun searchByEmail(email: String): UserEntity

    @Query("SELECT * FROM user_properties WHERE user_id = :id")
    suspend fun searchById(id: String): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReplace(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(user: UserEntity): Long

    @Query("UPDATE user_properties SET user_id = :user_id, email = :email")
    suspend fun updateUser(user_id: String, email: String)

    @Query("UPDATE user_properties SET token = :token WHERE user_id = :user_id")
    suspend fun updateUserToken(user_id: String, token: String)

    @Query("UPDATE user_properties SET isLoggedIn = :isLoggedIn WHERE user_id = :user_id")
    suspend fun updateLoggedIn(user_id: String, isLoggedIn: Boolean)
}
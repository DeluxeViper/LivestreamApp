package com.deluxe_viper.livestreamapp.business.datasource.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deluxe_viper.livestreamapp.business.datasource.cache.location.LocationDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.location.LocationInfoEntity
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserEntity

@Database(entities = [UserEntity::class, LocationInfoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserPropertiesDao(): UserDao

    abstract fun getLocationDao(): LocationDao

    companion object {
        val DATABASE_NAME: String = "livestream_database"
    }
}
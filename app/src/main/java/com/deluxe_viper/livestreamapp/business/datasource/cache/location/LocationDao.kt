package com.deluxe_viper.livestreamapp.business.datasource.cache.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations")
    suspend fun findAllLocations(): List<LocationInfoEntity>

    @Query("SELECT * FROM locations WHERE user_id = :user_id ")
    suspend fun getLocationByUser(user_id: String): LocationInfoEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReplace(user_id: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(user_id: String): Long

    @Query("UPDATE locations SET latitude = :latitude, longitude = :longitude WHERE user_id = :user_id")
    suspend fun updateLocation(user_id: String, latitude: Double, longitude: Double)
}
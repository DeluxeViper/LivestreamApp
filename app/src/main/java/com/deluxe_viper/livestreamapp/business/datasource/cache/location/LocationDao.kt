package com.deluxe_viper.livestreamapp.business.datasource.cache.location

import androidx.annotation.NonNull
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
    suspend fun insertAndReplace(location: LocationInfoEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(location: LocationInfoEntity): Long

    @Query("UPDATE locations SET latitude = :latitude, longitude = :longitude WHERE user_id = :user_id")
    suspend fun updateLocation(user_id: String, latitude: Double, longitude: Double)
}
package com.deluxe_viper.livestreamapp.business.datasource.cache.location

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserEntity
import com.deluxe_viper.livestreamapp.business.domain.models.LocationInfo

@Entity(tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class LocationInfoEntity(

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    var user_id: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double
)

fun LocationInfoEntity.toLocationInfo(): LocationInfo {

    return LocationInfo(
        user_id = user_id,
        latitude = latitude,
        longitude = longitude
    )
}

fun LocationInfo.toEntity(): LocationInfoEntity {
    return LocationInfoEntity(
        user_id = user_id,
        latitude = latitude,
        longitude = longitude
    )
}
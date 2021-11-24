package com.deluxe_viper.livestreamapp.business.datasource.cache.user

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deluxe_viper.livestreamapp.business.datasource.cache.location.LocationInfoEntity
import com.deluxe_viper.livestreamapp.business.datasource.cache.location.toEntity
import com.deluxe_viper.livestreamapp.business.datasource.cache.location.toLocationInfo
import com.deluxe_viper.livestreamapp.business.domain.models.User

@Entity(tableName = "user_properties")
data class UserEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "isLoggedIn")
    val isLoggedIn: Boolean,

    @ColumnInfo(name = "isStreaming")
    val isStreaming: Boolean,

    @ColumnInfo(name = "token")
    val token: String? = null,

    @Embedded
    val locationInfo: LocationInfoEntity? = null
)

fun UserEntity.toUser(): User {
    if (token == null) {
        throw Exception("User token cannot be null.")
    }

    return User(
        id = id,
        email = email,
        isLoggedIn = isLoggedIn,
        isStreaming =  isStreaming,
        locationInfo = locationInfo?.toLocationInfo(),
        authToken = token
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        isLoggedIn = isLoggedIn,
        isStreaming = isStreaming,
        locationInfo =  locationInfo?.toEntity(),
        token = authToken
    )
}
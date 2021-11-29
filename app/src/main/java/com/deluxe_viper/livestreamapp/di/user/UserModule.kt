package com.deluxe_viper.livestreamapp.di.user

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.interactors.user.GetUser
import com.deluxe_viper.livestreamapp.business.interactors.user.GetUsers
import com.deluxe_viper.livestreamapp.business.interactors.user.SubscribeToUsers
import com.deluxe_viper.livestreamapp.business.interactors.user.UpdateUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Singleton
    @Provides
    fun provideGetUser(
        service: ApiMainService,
        userDao: UserDao
    ): GetUser{
        return GetUser(service, userDao)
    }

    @Singleton
    @Provides
    fun provideGetUsers(
        service: ApiMainService,
        userDao: UserDao,
        ): GetUsers {
        return GetUsers(service, userDao)
    }

    @Singleton
    @Provides
    fun provideUpdateUser(
        service: ApiMainService,
        userDao: UserDao,
    ): UpdateUser {
        return UpdateUser(service, userDao)
    }

    @Singleton
    @Provides
    fun provideSubscribeToUsers(
        service: ApiMainService,
    ): SubscribeToUsers{
        return SubscribeToUsers(service)
    }
}
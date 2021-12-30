package com.deluxe_viper.livestreamapp.di.auth

import com.deluxe_viper.livestreamapp.business.datasource.cache.user.UserDao
import com.deluxe_viper.livestreamapp.business.datasource.datastore.AppDataStore
import com.deluxe_viper.livestreamapp.business.datasource.network.auth.AuthApiService
import com.deluxe_viper.livestreamapp.business.datasource.network.main.ApiMainService
import com.deluxe_viper.livestreamapp.business.interactors.auth.Login
import com.deluxe_viper.livestreamapp.business.interactors.auth.Logout
import com.deluxe_viper.livestreamapp.business.interactors.auth.Register
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit
import javax.inject.Singleton

@FlowPreview
@Module
@InstallIn(SingletonComponent::class)
class AuthModule {
    @Singleton
    @Provides
    fun provideAuthApiService(retrofitBuilder: Retrofit.Builder): AuthApiService {
        return retrofitBuilder
            .build()
            .create(AuthApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideLogin(
        service: AuthApiService,
        mainService: ApiMainService,
        userDao: UserDao,
        appDataStoreManager: AppDataStore,
    ): Login {
        return Login(
            service,
            mainService,
            userDao,
            appDataStoreManager
        )
    }

    @Singleton
    @Provides
    fun provideLogout(
        service: AuthApiService,
        appDataStoreManager: AppDataStore,
        userDao: UserDao
    ): Logout {
        return Logout(
            service,
            appDataStoreManager,
            userDao
        )
    }

    @Singleton
    @Provides
    fun provideRegister(
        service: AuthApiService,
    ): Register {
        return Register(
            service,
        )
    }
}
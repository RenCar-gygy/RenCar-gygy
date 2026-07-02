package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.auth.AuthRepository
import com.turkcell.rencarapp.data.auth.FakeAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}

package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.wallet.DefaultWalletRepository
import com.turkcell.rencarapp.data.wallet.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {
    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: DefaultWalletRepository): WalletRepository
}

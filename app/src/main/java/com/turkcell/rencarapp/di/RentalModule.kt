package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.rental.FakeRentalRepository
import com.turkcell.rencarapp.data.rental.RentalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RentalModule {
    @Binds
    @Singleton
    abstract fun bindRentalRepository(impl: FakeRentalRepository): RentalRepository
}

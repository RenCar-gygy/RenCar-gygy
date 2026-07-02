package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.vehicle.FakeVehicleRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VehicleModule {
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: FakeVehicleRepository): VehicleRepository
}

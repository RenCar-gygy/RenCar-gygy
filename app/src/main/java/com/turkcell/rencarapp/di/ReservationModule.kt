package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.reservation.DefaultReservationRepository
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReservationModule {

    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        impl: DefaultReservationRepository
    ): ReservationRepository
}

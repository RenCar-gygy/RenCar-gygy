package com.turkcell.rencarapp.di

import com.turkcell.rencarapp.data.license.FakeLicenseRepository
import com.turkcell.rencarapp.data.license.LicenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LicenseModule {
    @Binds
    @Singleton
    abstract fun bindLicenseRepository(impl: FakeLicenseRepository): LicenseRepository
}

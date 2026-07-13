package com.turkcell.rencarapp.ui.vehicle.detail;

import androidx.lifecycle.SavedStateHandle;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.turkcell.rencarapp.data.vehicle.VehicleRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class VehicleDetailViewModel_Factory implements Factory<VehicleDetailViewModel> {
  private final Provider<VehicleRepository> vehicleRepositoryProvider;

  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private VehicleDetailViewModel_Factory(Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.vehicleRepositoryProvider = vehicleRepositoryProvider;
    this.fusedLocationClientProvider = fusedLocationClientProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public VehicleDetailViewModel get() {
    return newInstance(vehicleRepositoryProvider.get(), fusedLocationClientProvider.get(), savedStateHandleProvider.get());
  }

  public static VehicleDetailViewModel_Factory create(
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new VehicleDetailViewModel_Factory(vehicleRepositoryProvider, fusedLocationClientProvider, savedStateHandleProvider);
  }

  public static VehicleDetailViewModel newInstance(VehicleRepository vehicleRepository,
      FusedLocationProviderClient fusedLocationClient, SavedStateHandle savedStateHandle) {
    return new VehicleDetailViewModel(vehicleRepository, fusedLocationClient, savedStateHandle);
  }
}

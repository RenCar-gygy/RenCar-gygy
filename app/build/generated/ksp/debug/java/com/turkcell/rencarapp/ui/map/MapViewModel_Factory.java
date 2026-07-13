package com.turkcell.rencarapp.ui.map;

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
public final class MapViewModel_Factory implements Factory<MapViewModel> {
  private final Provider<VehicleRepository> vehicleRepositoryProvider;

  private final Provider<MapAreaLabelResolver> areaLabelResolverProvider;

  private MapViewModel_Factory(Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<MapAreaLabelResolver> areaLabelResolverProvider) {
    this.vehicleRepositoryProvider = vehicleRepositoryProvider;
    this.areaLabelResolverProvider = areaLabelResolverProvider;
  }

  @Override
  public MapViewModel get() {
    return newInstance(vehicleRepositoryProvider.get(), areaLabelResolverProvider.get());
  }

  public static MapViewModel_Factory create(Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<MapAreaLabelResolver> areaLabelResolverProvider) {
    return new MapViewModel_Factory(vehicleRepositoryProvider, areaLabelResolverProvider);
  }

  public static MapViewModel newInstance(VehicleRepository vehicleRepository,
      MapAreaLabelResolver areaLabelResolver) {
    return new MapViewModel(vehicleRepository, areaLabelResolver);
  }
}

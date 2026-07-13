package com.turkcell.rencarapp.data.vehicle;

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor;
import com.turkcell.rencarapp.data.network.api.VehicleApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DefaultVehicleRepository_Factory implements Factory<DefaultVehicleRepository> {
  private final Provider<VehicleApi> vehicleApiProvider;

  private final Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider;

  private DefaultVehicleRepository_Factory(Provider<VehicleApi> vehicleApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    this.vehicleApiProvider = vehicleApiProvider;
    this.authorizedRequestExecutorProvider = authorizedRequestExecutorProvider;
  }

  @Override
  public DefaultVehicleRepository get() {
    return newInstance(vehicleApiProvider.get(), authorizedRequestExecutorProvider.get());
  }

  public static DefaultVehicleRepository_Factory create(Provider<VehicleApi> vehicleApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    return new DefaultVehicleRepository_Factory(vehicleApiProvider, authorizedRequestExecutorProvider);
  }

  public static DefaultVehicleRepository newInstance(VehicleApi vehicleApi,
      AuthorizedRequestExecutor authorizedRequestExecutor) {
    return new DefaultVehicleRepository(vehicleApi, authorizedRequestExecutor);
  }
}

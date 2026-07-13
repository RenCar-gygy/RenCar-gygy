package com.turkcell.rencarapp.data.network;

import com.turkcell.rencarapp.data.network.api.VehicleApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideVehicleApiFactory implements Factory<VehicleApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ProvideVehicleApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public VehicleApi get() {
    return provideVehicleApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideVehicleApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideVehicleApiFactory(retrofitProvider);
  }

  public static VehicleApi provideVehicleApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideVehicleApi(retrofit));
  }
}

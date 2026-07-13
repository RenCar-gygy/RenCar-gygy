package com.turkcell.rencarapp.data.network;

import com.turkcell.rencarapp.data.network.api.RentalApi;
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
public final class NetworkModule_ProvideRentalApiFactory implements Factory<RentalApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ProvideRentalApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public RentalApi get() {
    return provideRentalApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideRentalApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideRentalApiFactory(retrofitProvider);
  }

  public static RentalApi provideRentalApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideRentalApi(retrofit));
  }
}

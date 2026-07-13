package com.turkcell.rencarapp.data.vehicle;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class FakeVehicleRepository_Factory implements Factory<FakeVehicleRepository> {
  @Override
  public FakeVehicleRepository get() {
    return newInstance();
  }

  public static FakeVehicleRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeVehicleRepository newInstance() {
    return new FakeVehicleRepository();
  }

  private static final class InstanceHolder {
    static final FakeVehicleRepository_Factory INSTANCE = new FakeVehicleRepository_Factory();
  }
}

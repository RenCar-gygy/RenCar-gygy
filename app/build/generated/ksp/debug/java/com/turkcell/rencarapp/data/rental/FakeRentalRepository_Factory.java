package com.turkcell.rencarapp.data.rental;

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
public final class FakeRentalRepository_Factory implements Factory<FakeRentalRepository> {
  @Override
  public FakeRentalRepository get() {
    return newInstance();
  }

  public static FakeRentalRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeRentalRepository newInstance() {
    return new FakeRentalRepository();
  }

  private static final class InstanceHolder {
    static final FakeRentalRepository_Factory INSTANCE = new FakeRentalRepository_Factory();
  }
}

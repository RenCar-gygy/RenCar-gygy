package com.turkcell.rencarapp.data.license;

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
public final class FakeLicenseRepository_Factory implements Factory<FakeLicenseRepository> {
  @Override
  public FakeLicenseRepository get() {
    return newInstance();
  }

  public static FakeLicenseRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeLicenseRepository newInstance() {
    return new FakeLicenseRepository();
  }

  private static final class InstanceHolder {
    static final FakeLicenseRepository_Factory INSTANCE = new FakeLicenseRepository_Factory();
  }
}

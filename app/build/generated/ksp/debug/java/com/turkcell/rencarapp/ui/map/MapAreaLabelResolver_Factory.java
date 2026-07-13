package com.turkcell.rencarapp.ui.map;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MapAreaLabelResolver_Factory implements Factory<MapAreaLabelResolver> {
  private final Provider<Context> contextProvider;

  private MapAreaLabelResolver_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MapAreaLabelResolver get() {
    return newInstance(contextProvider.get());
  }

  public static MapAreaLabelResolver_Factory create(Provider<Context> contextProvider) {
    return new MapAreaLabelResolver_Factory(contextProvider);
  }

  public static MapAreaLabelResolver newInstance(Context context) {
    return new MapAreaLabelResolver(context);
  }
}

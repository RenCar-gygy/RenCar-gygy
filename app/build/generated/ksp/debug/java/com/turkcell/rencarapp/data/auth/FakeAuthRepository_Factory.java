package com.turkcell.rencarapp.data.auth;

import com.turkcell.rencarapp.data.session.SessionStore;
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
public final class FakeAuthRepository_Factory implements Factory<FakeAuthRepository> {
  private final Provider<SessionStore> sessionStoreProvider;

  private FakeAuthRepository_Factory(Provider<SessionStore> sessionStoreProvider) {
    this.sessionStoreProvider = sessionStoreProvider;
  }

  @Override
  public FakeAuthRepository get() {
    return newInstance(sessionStoreProvider.get());
  }

  public static FakeAuthRepository_Factory create(Provider<SessionStore> sessionStoreProvider) {
    return new FakeAuthRepository_Factory(sessionStoreProvider);
  }

  public static FakeAuthRepository newInstance(SessionStore sessionStore) {
    return new FakeAuthRepository(sessionStore);
  }
}

package com.turkcell.rencarapp.data.auth;

import com.turkcell.rencarapp.data.network.api.AuthApi;
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
public final class AuthorizedRequestExecutor_Factory implements Factory<AuthorizedRequestExecutor> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<SessionStore> sessionStoreProvider;

  private AuthorizedRequestExecutor_Factory(Provider<AuthApi> authApiProvider,
      Provider<SessionStore> sessionStoreProvider) {
    this.authApiProvider = authApiProvider;
    this.sessionStoreProvider = sessionStoreProvider;
  }

  @Override
  public AuthorizedRequestExecutor get() {
    return newInstance(authApiProvider.get(), sessionStoreProvider.get());
  }

  public static AuthorizedRequestExecutor_Factory create(Provider<AuthApi> authApiProvider,
      Provider<SessionStore> sessionStoreProvider) {
    return new AuthorizedRequestExecutor_Factory(authApiProvider, sessionStoreProvider);
  }

  public static AuthorizedRequestExecutor newInstance(AuthApi authApi, SessionStore sessionStore) {
    return new AuthorizedRequestExecutor(authApi, sessionStore);
  }
}

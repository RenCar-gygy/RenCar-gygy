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
public final class DefaultAuthRepository_Factory implements Factory<DefaultAuthRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<SessionStore> sessionStoreProvider;

  private final Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider;

  private DefaultAuthRepository_Factory(Provider<AuthApi> authApiProvider,
      Provider<SessionStore> sessionStoreProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    this.authApiProvider = authApiProvider;
    this.sessionStoreProvider = sessionStoreProvider;
    this.authorizedRequestExecutorProvider = authorizedRequestExecutorProvider;
  }

  @Override
  public DefaultAuthRepository get() {
    return newInstance(authApiProvider.get(), sessionStoreProvider.get(), authorizedRequestExecutorProvider.get());
  }

  public static DefaultAuthRepository_Factory create(Provider<AuthApi> authApiProvider,
      Provider<SessionStore> sessionStoreProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    return new DefaultAuthRepository_Factory(authApiProvider, sessionStoreProvider, authorizedRequestExecutorProvider);
  }

  public static DefaultAuthRepository newInstance(AuthApi authApi, SessionStore sessionStore,
      AuthorizedRequestExecutor authorizedRequestExecutor) {
    return new DefaultAuthRepository(authApi, sessionStore, authorizedRequestExecutor);
  }
}

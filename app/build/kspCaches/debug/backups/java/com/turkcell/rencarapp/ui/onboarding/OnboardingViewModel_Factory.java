package com.turkcell.rencarapp.ui.onboarding;

import com.turkcell.rencarapp.data.session.SessionStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<SessionStore> sessionStoreProvider;

  private OnboardingViewModel_Factory(Provider<SessionStore> sessionStoreProvider) {
    this.sessionStoreProvider = sessionStoreProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(sessionStoreProvider.get());
  }

  public static OnboardingViewModel_Factory create(Provider<SessionStore> sessionStoreProvider) {
    return new OnboardingViewModel_Factory(sessionStoreProvider);
  }

  public static OnboardingViewModel newInstance(SessionStore sessionStore) {
    return new OnboardingViewModel(sessionStore);
  }
}

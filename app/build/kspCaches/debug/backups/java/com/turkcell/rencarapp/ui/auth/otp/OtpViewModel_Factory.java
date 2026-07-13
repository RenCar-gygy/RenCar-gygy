package com.turkcell.rencarapp.ui.auth.otp;

import androidx.lifecycle.SavedStateHandle;
import com.turkcell.rencarapp.data.auth.AuthRepository;
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
public final class OtpViewModel_Factory implements Factory<OtpViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private OtpViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public OtpViewModel get() {
    return newInstance(savedStateHandleProvider.get(), authRepositoryProvider.get());
  }

  public static OtpViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new OtpViewModel_Factory(savedStateHandleProvider, authRepositoryProvider);
  }

  public static OtpViewModel newInstance(SavedStateHandle savedStateHandle,
      AuthRepository authRepository) {
    return new OtpViewModel(savedStateHandle, authRepository);
  }
}

package com.turkcell.rencarapp;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor;
import com.turkcell.rencarapp.data.auth.DefaultAuthRepository;
import com.turkcell.rencarapp.data.license.DefaultLicenseRepository;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideAuthApiFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideJsonFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideLicenseApiFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideOkHttpClientFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideRentalApiFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideRetrofitFactory;
import com.turkcell.rencarapp.data.network.NetworkModule_ProvideVehicleApiFactory;
import com.turkcell.rencarapp.data.network.api.AuthApi;
import com.turkcell.rencarapp.data.network.api.LicenseApi;
import com.turkcell.rencarapp.data.network.api.RentalApi;
import com.turkcell.rencarapp.data.network.api.VehicleApi;
import com.turkcell.rencarapp.data.rental.DefaultRentalRepository;
import com.turkcell.rencarapp.data.session.DataStoreSessionStore;
import com.turkcell.rencarapp.data.vehicle.DefaultVehicleRepository;
import com.turkcell.rencarapp.di.LocationModule_ProvideFusedLocationProviderClientFactory;
import com.turkcell.rencarapp.ui.auth.login.LoginViewModel;
import com.turkcell.rencarapp.ui.auth.login.LoginViewModel_HiltModules;
import com.turkcell.rencarapp.ui.auth.login.LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.auth.login.LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.auth.otp.OtpViewModel;
import com.turkcell.rencarapp.ui.auth.otp.OtpViewModel_HiltModules;
import com.turkcell.rencarapp.ui.auth.otp.OtpViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.auth.otp.OtpViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.auth.register.RegisterViewModel;
import com.turkcell.rencarapp.ui.auth.register.RegisterViewModel_HiltModules;
import com.turkcell.rencarapp.ui.auth.register.RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.auth.register.RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.license.LicenseViewModel;
import com.turkcell.rencarapp.ui.license.LicenseViewModel_HiltModules;
import com.turkcell.rencarapp.ui.license.LicenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.license.LicenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.map.MapAreaLabelResolver;
import com.turkcell.rencarapp.ui.map.MapViewModel;
import com.turkcell.rencarapp.ui.map.MapViewModel_HiltModules;
import com.turkcell.rencarapp.ui.map.MapViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.map.MapViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.onboarding.OnboardingViewModel;
import com.turkcell.rencarapp.ui.onboarding.OnboardingViewModel_HiltModules;
import com.turkcell.rencarapp.ui.onboarding.OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.onboarding.OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.payment.wallet.WalletViewModel;
import com.turkcell.rencarapp.ui.payment.wallet.WalletViewModel_HiltModules;
import com.turkcell.rencarapp.ui.payment.wallet.WalletViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.payment.wallet.WalletViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.profile.ProfileViewModel;
import com.turkcell.rencarapp.ui.profile.ProfileViewModel_HiltModules;
import com.turkcell.rencarapp.ui.profile.ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.profile.ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel;
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel_HiltModules;
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationViewModel;
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationViewModel_HiltModules;
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosViewModel;
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosViewModel_HiltModules;
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryViewModel;
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryViewModel_HiltModules;
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.history.RentalHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryViewModel;
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryViewModel_HiltModules;
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.rental.summary.RentalSummaryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.splash.SplashViewModel;
import com.turkcell.rencarapp.ui.splash.SplashViewModel_HiltModules;
import com.turkcell.rencarapp.ui.splash.SplashViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.splash.SplashViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailViewModel;
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailViewModel_HiltModules;
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerRenCarApplication_HiltComponents_SingletonC {
  private DaggerRenCarApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public RenCarApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements RenCarApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements RenCarApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements RenCarApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements RenCarApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements RenCarApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements RenCarApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements RenCarApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public RenCarApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends RenCarApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends RenCarApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends RenCarApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends RenCarApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(15);
      mapBuilder.put(ActiveRentalViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ActiveRentalViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(DeliveryPhotosViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DeliveryPhotosViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(LicenseViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LicenseViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MapViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MapViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OnboardingViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(OtpViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OtpViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RegisterViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(RentalConfirmationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RentalConfirmationViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(RentalHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RentalHistoryViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(RentalSummaryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RentalSummaryViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SplashViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SplashViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(VehicleDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, VehicleDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(WalletViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, WalletViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends RenCarApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<ActiveRentalViewModel> activeRentalViewModelProvider;

    Provider<DeliveryPhotosViewModel> deliveryPhotosViewModelProvider;

    Provider<LicenseViewModel> licenseViewModelProvider;

    Provider<LoginViewModel> loginViewModelProvider;

    Provider<MapViewModel> mapViewModelProvider;

    Provider<OnboardingViewModel> onboardingViewModelProvider;

    Provider<OtpViewModel> otpViewModelProvider;

    Provider<ProfileViewModel> profileViewModelProvider;

    Provider<RegisterViewModel> registerViewModelProvider;

    Provider<RentalConfirmationViewModel> rentalConfirmationViewModelProvider;

    Provider<RentalHistoryViewModel> rentalHistoryViewModelProvider;

    Provider<RentalSummaryViewModel> rentalSummaryViewModelProvider;

    Provider<SplashViewModel> splashViewModelProvider;

    Provider<VehicleDetailViewModel> vehicleDetailViewModelProvider;

    Provider<WalletViewModel> walletViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    MapAreaLabelResolver mapAreaLabelResolver() {
      return new MapAreaLabelResolver(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    Map hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(15);
      mapBuilder.put(ActiveRentalViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (activeRentalViewModelProvider)));
      mapBuilder.put(DeliveryPhotosViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (deliveryPhotosViewModelProvider)));
      mapBuilder.put(LicenseViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (licenseViewModelProvider)));
      mapBuilder.put(LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (loginViewModelProvider)));
      mapBuilder.put(MapViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (mapViewModelProvider)));
      mapBuilder.put(OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (onboardingViewModelProvider)));
      mapBuilder.put(OtpViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (otpViewModelProvider)));
      mapBuilder.put(ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (profileViewModelProvider)));
      mapBuilder.put(RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (registerViewModelProvider)));
      mapBuilder.put(RentalConfirmationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (rentalConfirmationViewModelProvider)));
      mapBuilder.put(RentalHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (rentalHistoryViewModelProvider)));
      mapBuilder.put(RentalSummaryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (rentalSummaryViewModelProvider)));
      mapBuilder.put(SplashViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (splashViewModelProvider)));
      mapBuilder.put(VehicleDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (vehicleDetailViewModelProvider)));
      mapBuilder.put(WalletViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (walletViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.activeRentalViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.deliveryPhotosViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.licenseViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.mapViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.otpViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.rentalConfirmationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.rentalHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.rentalSummaryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.vehicleDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.walletViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.turkcell.rencarapp.ui.rental.active.ActiveRentalViewModel
          return (T) new ActiveRentalViewModel(singletonCImpl.defaultRentalRepositoryProvider.get(), singletonCImpl.defaultVehicleRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 1: // com.turkcell.rencarapp.ui.rental.delivery_photos.DeliveryPhotosViewModel
          return (T) new DeliveryPhotosViewModel(singletonCImpl.defaultRentalRepositoryProvider.get(), singletonCImpl.defaultVehicleRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 2: // com.turkcell.rencarapp.ui.license.LicenseViewModel
          return (T) new LicenseViewModel(singletonCImpl.defaultLicenseRepositoryProvider.get(), singletonCImpl.defaultAuthRepositoryProvider.get(), singletonCImpl.dataStoreSessionStoreProvider.get());

          case 3: // com.turkcell.rencarapp.ui.auth.login.LoginViewModel
          return (T) new LoginViewModel(singletonCImpl.defaultAuthRepositoryProvider.get());

          case 4: // com.turkcell.rencarapp.ui.map.MapViewModel
          return (T) new MapViewModel(singletonCImpl.defaultVehicleRepositoryProvider.get(), viewModelCImpl.mapAreaLabelResolver());

          case 5: // com.turkcell.rencarapp.ui.onboarding.OnboardingViewModel
          return (T) new OnboardingViewModel(singletonCImpl.dataStoreSessionStoreProvider.get());

          case 6: // com.turkcell.rencarapp.ui.auth.otp.OtpViewModel
          return (T) new OtpViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.defaultAuthRepositoryProvider.get());

          case 7: // com.turkcell.rencarapp.ui.profile.ProfileViewModel
          return (T) new ProfileViewModel(singletonCImpl.defaultAuthRepositoryProvider.get());

          case 8: // com.turkcell.rencarapp.ui.auth.register.RegisterViewModel
          return (T) new RegisterViewModel(singletonCImpl.defaultAuthRepositoryProvider.get());

          case 9: // com.turkcell.rencarapp.ui.rental.confirmation.RentalConfirmationViewModel
          return (T) new RentalConfirmationViewModel(singletonCImpl.defaultVehicleRepositoryProvider.get(), singletonCImpl.defaultRentalRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 10: // com.turkcell.rencarapp.ui.rental.history.RentalHistoryViewModel
          return (T) new RentalHistoryViewModel(singletonCImpl.defaultRentalRepositoryProvider.get());

          case 11: // com.turkcell.rencarapp.ui.rental.summary.RentalSummaryViewModel
          return (T) new RentalSummaryViewModel(singletonCImpl.defaultRentalRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 12: // com.turkcell.rencarapp.ui.splash.SplashViewModel
          return (T) new SplashViewModel(singletonCImpl.defaultAuthRepositoryProvider.get(), singletonCImpl.dataStoreSessionStoreProvider.get());

          case 13: // com.turkcell.rencarapp.ui.vehicle.detail.VehicleDetailViewModel
          return (T) new VehicleDetailViewModel(singletonCImpl.defaultVehicleRepositoryProvider.get(), singletonCImpl.provideFusedLocationProviderClientProvider.get(), viewModelCImpl.savedStateHandle);

          case 14: // com.turkcell.rencarapp.ui.payment.wallet.WalletViewModel
          return (T) new WalletViewModel();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends RenCarApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends RenCarApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends RenCarApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<Json> provideJsonProvider;

    Provider<OkHttpClient> provideOkHttpClientProvider;

    Provider<Retrofit> provideRetrofitProvider;

    Provider<RentalApi> provideRentalApiProvider;

    Provider<AuthApi> provideAuthApiProvider;

    Provider<DataStoreSessionStore> dataStoreSessionStoreProvider;

    Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider;

    Provider<DefaultRentalRepository> defaultRentalRepositoryProvider;

    Provider<VehicleApi> provideVehicleApiProvider;

    Provider<DefaultVehicleRepository> defaultVehicleRepositoryProvider;

    Provider<LicenseApi> provideLicenseApiProvider;

    Provider<DefaultLicenseRepository> defaultLicenseRepositoryProvider;

    Provider<DefaultAuthRepository> defaultAuthRepositoryProvider;

    Provider<FusedLocationProviderClient> provideFusedLocationProviderClientProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 3));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 4));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 2));
      this.provideRentalApiProvider = DoubleCheck.provider(new SwitchingProvider<RentalApi>(singletonCImpl, 1));
      this.provideAuthApiProvider = DoubleCheck.provider(new SwitchingProvider<AuthApi>(singletonCImpl, 6));
      this.dataStoreSessionStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStoreSessionStore>(singletonCImpl, 7));
      this.authorizedRequestExecutorProvider = DoubleCheck.provider(new SwitchingProvider<AuthorizedRequestExecutor>(singletonCImpl, 5));
      this.defaultRentalRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DefaultRentalRepository>(singletonCImpl, 0));
      this.provideVehicleApiProvider = DoubleCheck.provider(new SwitchingProvider<VehicleApi>(singletonCImpl, 9));
      this.defaultVehicleRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DefaultVehicleRepository>(singletonCImpl, 8));
      this.provideLicenseApiProvider = DoubleCheck.provider(new SwitchingProvider<LicenseApi>(singletonCImpl, 11));
      this.defaultLicenseRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DefaultLicenseRepository>(singletonCImpl, 10));
      this.defaultAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DefaultAuthRepository>(singletonCImpl, 12));
      this.provideFusedLocationProviderClientProvider = DoubleCheck.provider(new SwitchingProvider<FusedLocationProviderClient>(singletonCImpl, 13));
    }

    @Override
    public void injectRenCarApplication(RenCarApplication renCarApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.turkcell.rencarapp.data.rental.DefaultRentalRepository
          return (T) new DefaultRentalRepository(singletonCImpl.provideRentalApiProvider.get(), singletonCImpl.authorizedRequestExecutorProvider.get());

          case 1: // com.turkcell.rencarapp.data.network.api.RentalApi
          return (T) NetworkModule_ProvideRentalApiFactory.provideRentalApi(singletonCImpl.provideRetrofitProvider.get());

          case 2: // retrofit2.Retrofit
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideJsonProvider.get(), singletonCImpl.provideOkHttpClientProvider.get());

          case 3: // kotlinx.serialization.json.Json
          return (T) NetworkModule_ProvideJsonFactory.provideJson();

          case 4: // okhttp3.OkHttpClient
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 5: // com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
          return (T) new AuthorizedRequestExecutor(singletonCImpl.provideAuthApiProvider.get(), singletonCImpl.dataStoreSessionStoreProvider.get());

          case 6: // com.turkcell.rencarapp.data.network.api.AuthApi
          return (T) NetworkModule_ProvideAuthApiFactory.provideAuthApi(singletonCImpl.provideRetrofitProvider.get());

          case 7: // com.turkcell.rencarapp.data.session.DataStoreSessionStore
          return (T) new DataStoreSessionStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.turkcell.rencarapp.data.vehicle.DefaultVehicleRepository
          return (T) new DefaultVehicleRepository(singletonCImpl.provideVehicleApiProvider.get(), singletonCImpl.authorizedRequestExecutorProvider.get());

          case 9: // com.turkcell.rencarapp.data.network.api.VehicleApi
          return (T) NetworkModule_ProvideVehicleApiFactory.provideVehicleApi(singletonCImpl.provideRetrofitProvider.get());

          case 10: // com.turkcell.rencarapp.data.license.DefaultLicenseRepository
          return (T) new DefaultLicenseRepository(singletonCImpl.provideLicenseApiProvider.get(), singletonCImpl.authorizedRequestExecutorProvider.get());

          case 11: // com.turkcell.rencarapp.data.network.api.LicenseApi
          return (T) NetworkModule_ProvideLicenseApiFactory.provideLicenseApi(singletonCImpl.provideRetrofitProvider.get());

          case 12: // com.turkcell.rencarapp.data.auth.DefaultAuthRepository
          return (T) new DefaultAuthRepository(singletonCImpl.provideAuthApiProvider.get(), singletonCImpl.dataStoreSessionStoreProvider.get(), singletonCImpl.authorizedRequestExecutorProvider.get());

          case 13: // com.google.android.gms.location.FusedLocationProviderClient
          return (T) LocationModule_ProvideFusedLocationProviderClientFactory.provideFusedLocationProviderClient(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}

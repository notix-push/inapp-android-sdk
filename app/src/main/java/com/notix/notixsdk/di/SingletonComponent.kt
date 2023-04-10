package com.notix.notixsdk.di

import com.notix.notixsdk.NotixCallbackReporter
import com.notix.notixsdk.di.contextprovider.ContextProvider
import com.notix.notixsdk.di.contextprovider.ContextProviderImpl
import com.notix.notixsdk.di.contextprovider.ContextProviderInitializer
import com.notix.notixsdk.data.EventDataSource
import com.notix.notixsdk.data.EventDataSourceImpl
import com.notix.notixsdk.data.EventRepository
import com.notix.notixsdk.data.EventRepositoryImpl
import com.notix.notixsdk.data.MainDataSource
import com.notix.notixsdk.data.MainDataSourceImpl
import com.notix.notixsdk.data.MainRepository
import com.notix.notixsdk.data.MainRepositoryImpl
import com.notix.notixsdk.data.MetricsDataSource
import com.notix.notixsdk.data.MetricsDataSourceImpl
import com.notix.notixsdk.data.MetricsRepository
import com.notix.notixsdk.data.MetricsRepositoryImpl
import com.notix.notixsdk.interstitial.data.InterstitialDataSource
import com.notix.notixsdk.interstitial.data.InterstitialDataSourceImpl
import com.notix.notixsdk.interstitial.data.InterstitialRepository
import com.notix.notixsdk.interstitial.data.InterstitialRepositoryImpl
import com.notix.notixsdk.network.OkHttpClientProvider
import com.notix.notixsdk.push.permissions.NotificationsPermissionController
import com.notix.notixsdk.perseverance.EventReporter
import com.notix.notixsdk.perseverance.EventReporterImpl
import com.notix.notixsdk.perseverance.WorkerDataStore
import com.notix.notixsdk.perseverance.worker.SendEventsWorkerLauncher
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.push.UpdateSubscriptionStateUseCase
import com.notix.notixsdk.push.data.PushDataSource
import com.notix.notixsdk.push.data.PushDataSourceImpl
import com.notix.notixsdk.push.data.PushRepository
import com.notix.notixsdk.push.data.PushRepositoryImpl
import com.notix.notixsdk.push.firebase.FetchFcmTokenUseCase
import com.notix.notixsdk.push.firebase.FirebaseInstanceProvider

object SingletonComponent {
    private val _contextProvider = ContextProviderImpl()
    val contextProvider: ContextProvider = _contextProvider
    val contextProviderInitializer: ContextProviderInitializer = _contextProvider

    val csProvider = CoroutineScopesProvider()

    val notificationsPermissionController = NotificationsPermissionController(contextProvider)

    val storage = StorageProvider()

    val workerDataStore = WorkerDataStore(contextProvider)

    val sendEventsWorkerLauncher =
        SendEventsWorkerLauncher(contextProvider, csProvider.provideSupervisedIo())

    private val httpClient = OkHttpClientProvider().provide(contextProvider, storage)

    val notixCallbackReporter = NotixCallbackReporter()

    private val mainDataSourceImpl = MainDataSourceImpl(httpClient)
    val mainDataSource: MainDataSource = mainDataSourceImpl

    private val eventDataSourceImpl = EventDataSourceImpl(httpClient, storage)
    val eventDataSource: EventDataSource = eventDataSourceImpl

    private val pushDataSourceImpl = PushDataSourceImpl(httpClient)
    val pushDataSource: PushDataSource = pushDataSourceImpl

    private val interstitialDataSourceImpl = InterstitialDataSourceImpl(httpClient)
    val interstitialDataSource: InterstitialDataSource = interstitialDataSourceImpl

    private val metricsDataSourceImpl = MetricsDataSourceImpl(httpClient)
    val metricsDataSource: MetricsDataSource = metricsDataSourceImpl

    private val eventReporterImpl = EventReporterImpl(
        workerDataStore = workerDataStore,
        workerLauncher = sendEventsWorkerLauncher,
        cs = csProvider.provideSupervisedIo()
    )
    val eventReporter: EventReporter = eventReporterImpl

    private val mainRepositoryImpl = MainRepositoryImpl(
        mainDataSource = mainDataSource,
        storage = storage,
        contextProvider = contextProvider,
        cs = csProvider.provideSupervisedIo(),
        notixCallbackReporter = notixCallbackReporter
    )
    val mainRepository: MainRepository = mainRepositoryImpl

    private val eventRepositoryImpl = EventRepositoryImpl(
        eventDataSource = eventDataSource,
        notixCallbackReporter = notixCallbackReporter,
        cs = csProvider.provideSupervisedIo()
    )
    val eventRepository: EventRepository = eventRepositoryImpl

    private val pushRepositoryImpl = PushRepositoryImpl(
        pushDataSource = pushDataSource,
        storage = storage,
        contextProvider = contextProvider,
        cs = csProvider.provideSupervisedIo(),
        notixCallbackReporter = notixCallbackReporter
    )
    val pushRepository: PushRepository = pushRepositoryImpl

    private val metricsRepositoryImpl = MetricsRepositoryImpl(
        storage = storage,
        contextProvider = contextProvider,
        notificationsPermissionController = notificationsPermissionController,
        metricsDataSource = metricsDataSource,
        notixCallbackReporter = notixCallbackReporter
    )
    val metricsRepository: MetricsRepository = metricsRepositoryImpl

    private val interstitialRepositoryImpl = InterstitialRepositoryImpl(
        storage = storage,
        interstitialDataSource = interstitialDataSource,
        cs = csProvider.provideSupervisedIo()
    )
    val interstitialRepository: InterstitialRepository = interstitialRepositoryImpl

    val firebaseInstanceProvider = FirebaseInstanceProvider(contextProvider, storage)
    val fetchFcmTokenUseCase
        get() = FetchFcmTokenUseCase(
            firebaseInstanceProvider = firebaseInstanceProvider,
            notixCallbackReporter = notixCallbackReporter
        )
    val updateSubscriptionStateUseCase
        get() = UpdateSubscriptionStateUseCase(
            fetchFcmTokenUseCase = fetchFcmTokenUseCase,
            notificationsPermissionController = notificationsPermissionController,
            storage = storage,
            pushRepository = pushRepository,
            cs = csProvider.provideSupervisedIo()
        )
}
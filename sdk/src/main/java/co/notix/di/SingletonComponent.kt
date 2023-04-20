package co.notix.di

import co.notix.Notix
import co.notix.NotixImpl
import co.notix.audience.AudienceReporterImpl
import co.notix.audience.NotixAudienceReporter
import co.notix.callback.NotixCallbackReporter
import co.notix.data.EventDataSource
import co.notix.data.EventDataSourceImpl
import co.notix.data.EventRepository
import co.notix.data.EventRepositoryImpl
import co.notix.data.MainDataSource
import co.notix.data.MainDataSourceImpl
import co.notix.data.MainRepository
import co.notix.data.MainRepositoryImpl
import co.notix.data.MetricsDataSource
import co.notix.data.MetricsDataSourceImpl
import co.notix.data.MetricsRepository
import co.notix.data.MetricsRepositoryImpl
import co.notix.data.Storage
import co.notix.di.contextprovider.ContextProvider
import co.notix.di.contextprovider.ContextProviderImpl
import co.notix.di.contextprovider.ContextProviderInitializer
import co.notix.interstitial.data.InterstitialDataSource
import co.notix.interstitial.data.InterstitialDataSourceImpl
import co.notix.interstitial.data.InterstitialRepository
import co.notix.interstitial.data.InterstitialRepositoryImpl
import co.notix.network.OkHttpClientProvider
import co.notix.perseverance.EventReporter
import co.notix.perseverance.EventReporterImpl
import co.notix.perseverance.WorkerDataStore
import co.notix.perseverance.worker.SendEventsWorkerLauncher
import co.notix.push.NotificationHandler
import co.notix.push.NotificationHandlerImpl
import co.notix.push.NotificationModifierProvider
import co.notix.push.NotificationModifierProviderImpl
import co.notix.push.NotificationModifierProviderInitializer
import co.notix.push.NotificationReceiver
import co.notix.push.NotificationReceiverImpl
import co.notix.push.TargetEventHandlerProvider
import co.notix.push.TargetEventHandlerProviderImpl
import co.notix.push.TargetEventHandlerProviderInitializer
import co.notix.push.UpdateSubscriptionStateUseCase
import co.notix.push.data.PushDataSource
import co.notix.push.data.PushDataSourceImpl
import co.notix.push.data.PushRepository
import co.notix.push.data.PushRepositoryImpl
import co.notix.push.firebase.FetchFcmTokenUseCase
import co.notix.push.firebase.FirebaseInstanceProvider
import co.notix.push.permissions.NotificationsPermissionController
import co.notix.services.MetricsService

internal object SingletonComponent {
    private val _contextProvider = ContextProviderImpl()
    val contextProvider: ContextProvider = _contextProvider
    val contextProviderInitializer: ContextProviderInitializer = _contextProvider

    val storage = Storage()

    val csProvider = CoroutineScopesProvider()

    val notificationsPermissionController = NotificationsPermissionController(contextProvider)

    private val _notificationModifierProviderImpl = NotificationModifierProviderImpl(storage)
    internal val notificationModifierProviderInitializer: NotificationModifierProviderInitializer =
        _notificationModifierProviderImpl
    internal val notificationModifierProvider: NotificationModifierProvider =
        _notificationModifierProviderImpl

    private val _targetEventHandlerProviderImpl = TargetEventHandlerProviderImpl(storage)
    internal val targetEventHandlerProviderInitializer: TargetEventHandlerProviderInitializer =
        _targetEventHandlerProviderImpl
    internal val targetEventHandlerProvider: TargetEventHandlerProvider =
        _targetEventHandlerProviderImpl

    val workerDataStore = WorkerDataStore(contextProvider)

    val sendEventsWorkerLauncher =
        SendEventsWorkerLauncher(contextProvider, csProvider.provideSupervisedIo())

    private val httpClient = OkHttpClientProvider().provide(contextProvider, storage)

    val notixCallbackReporter = NotixCallbackReporter(contextProvider)

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

    private val notificationHandlerImpl = NotificationHandlerImpl(
        eventReporter,
        pushRepository,
        contextProvider,
        notificationModifierProvider
    )
    val notificationHandler: NotificationHandler = notificationHandlerImpl

    private val notificationReporterImpl = NotificationReceiverImpl(
        notificationsPermissionController = notificationsPermissionController,
        contextProvider = contextProvider,
        workerDataStore = workerDataStore,
        workerLauncher = sendEventsWorkerLauncher,
        csIo = csProvider.provideSupervisedIo()
    )
    val notificationReceiver: NotificationReceiver = notificationReporterImpl

    private val audienceReporterImpl = AudienceReporterImpl(
        mainRepository = mainRepository,
        csIo = csProvider.provideSupervisedIo()
    )
    val notixAudienceReporter: NotixAudienceReporter = audienceReporterImpl

    val notixImpl = NotixImpl(
        storage = storage,
        metricsService = MetricsService(),
        notificationsPermissionController = notificationsPermissionController,
        contextProvider = contextProvider,
        contextProviderInitializer = contextProviderInitializer,
        workerLauncher = sendEventsWorkerLauncher,
        mainRepository = mainRepository,
        csIo = csProvider.provideSupervisedIo(),
        notixCallbackReporter = notixCallbackReporter,
        updateSubscriptionStateUseCase = updateSubscriptionStateUseCase,
        metricsRepository = metricsRepository,
        notificationModifierProviderInitializer = notificationModifierProviderInitializer,
        targetEventHandlerProviderInitializer = targetEventHandlerProviderInitializer,
        notixAudienceReporter = notixAudienceReporter
    )
    val notix: Notix = notixImpl
}
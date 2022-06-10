package clickstream.eventvisualiser.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.*
import androidx.annotation.RequiresApi
import clickstream.eventvisualiser.ui.internal.CSEvActivityLifecycleCallback
import clickstream.eventvisualiser.ui.internal.data.datasource.CSEvDatasourceImpl
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepository
import clickstream.eventvisualiser.ui.internal.data.repository.CSEvRepositoryImpl
import clickstream.eventvisualiser.ui.internal.ui.activity.CSEvHomeActivity
import clickstream.eventvisualiser.ui.internal.ui.fragment.CSEvActionBottomSheet
import clickstream.eventvisualiser.ui.internal.ui.update
import clickstream.eventvisualiser.ui.internal.ui.views.CSEvFloatingWindow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/***
 * Main entry point to Event visualiser.
 * @see [initialise] to initialise and [getInstance] to create and get an instance.
 * @see show to show ev window.
 *
 * */
@RequiresApi(Build.VERSION_CODES.O)
public class CSEventVisualiserUI private constructor(
    private val application: Application,
    private val csEvRepository: CSEvRepository
) {

    private val activityLifecycleCallback by lazy { CSEvActivityLifecycleCallback() }

    private val csEvStateFlow =
        MutableStateFlow(CSEvFloatingWindow.State.CLOSE)

    private var coroutineScope: CoroutineScope? = null

    private var isActionBottomSheetShown = false

    init {
        registerActivityLifecycleCallback()
    }

    /**
     * Call this to show ev window and start capturing events.
     *
     * */
    public fun show() {
        if (!isEvWindowAlreadyShowing()) {
            initialiseCoroutinesScope()
            startCapture()
            createFloatingWindow()
        }
    }

    /**
     * Call this to hide ev window and stop capturing events.
     *
     * */
    public fun close() {
        csEvRepository.stopObserving()
        coroutineScope?.launch {
            clearData()
            csEvStateFlow.update { CSEvFloatingWindow.State.CLOSE }
            delay(100)
            cancelCoroutineScope()
        }
    }

    internal fun startCapture() {
        csEvRepository.startObserving()
        csEvStateFlow.update { CSEvFloatingWindow.State.ACTIVE }
    }

    internal fun stopCapture() {
        csEvRepository.stopObserving()
        csEvStateFlow.update { CSEvFloatingWindow.State.DEACTIVE }
    }

    internal fun setVisibility(isVisible: Boolean) {
        csEvStateFlow.update {
            if (isVisible)
                CSEvFloatingWindow.State.SHOW
            else
                CSEvFloatingWindow.State.HIDE
        }
    }

    internal suspend fun clearData() {
        csEvRepository.clearData()
    }

    internal fun release() {
        unregisterActivityLifecycleCallback()
        close()
    }


    private fun initialiseCoroutinesScope() {
        if (coroutineScope?.isActive != true) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }
    }

    private fun cancelCoroutineScope() {
        if (coroutineScope?.isActive == true) {
            coroutineScope?.cancel()
        }
    }

    private fun createFloatingWindow() =
        CSEvFloatingWindow(application).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            scope = coroutineScope
            layoutInflater = LayoutInflater.from(application.applicationContext)
            windowStateFlow = csEvStateFlow
            onSettingsClick = ::showActionBottomSheetIfNotShown
            setOnClickListener {
                setVisibility(false)
                goToCSEvHomeScreen(activityLifecycleCallback.getCurrentActivity())
            }
        }.addToWindow()

    private fun goToCSEvHomeScreen(context: Context) {
        context.startActivity(Intent(context, CSEvHomeActivity::class.java))
    }

    private fun isEvWindowAlreadyShowing(): Boolean {
        return csEvStateFlow.value != CSEvFloatingWindow.State.CLOSE
    }

    private fun registerActivityLifecycleCallback() {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)
    }

    private fun unregisterActivityLifecycleCallback() {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback)
    }

    private fun showActionBottomSheetIfNotShown() {
        if (!isActionBottomSheetShown) {
            isActionBottomSheetShown = true
            showActionBottomSheet()
        }
    }

    private fun showActionBottomSheet() {
        CSEvActionBottomSheet(activityLifecycleCallback.getCurrentActivity()).apply {
            actionListener = getBottomSheetActionCallback()
            setOnDismissListener {
                isActionBottomSheetShown = false
            }
        }.show()
    }

    private fun getBottomSheetActionCallback() =
        object : CSEvActionBottomSheet.CSEvActionCallbacks {
            override suspend fun startCapture() {
                this@CSEventVisualiserUI.startCapture()
            }

            override suspend fun stopCapture() {
                this@CSEventVisualiserUI.stopCapture()
            }

            override suspend fun clearData() {
                this@CSEventVisualiserUI.clearData()
            }

            override suspend fun close() {
                this@CSEventVisualiserUI.close()

            }

        }

    public companion object {

        @Volatile
        private var INSTANCE: CSEventVisualiserUI? = null
        private val lock = Any()

        /**
         * Call this to initialise CSEventVisualiserUI.
         * Ideal place to initialise is [Application.onCreate].
         *
         * */
        public fun initialise(application: Application) {
            if (INSTANCE == null) {
                synchronized(lock) {
                    if (INSTANCE == null) {
                        INSTANCE =
                            CSEventVisualiserUI(
                                application,
                                CSEvRepositoryImpl(CSEvDatasourceImpl.getInstance())
                            )
                    }
                }
            }
        }

        /**
         * Creates an static instance of CSEventVisualiserUI.
         * Make sure to call [initialise] in [Application.onCreate] before calling this method.
         *
         * */
        public fun getInstance(): CSEventVisualiserUI {
            if (INSTANCE == null) {
                throw IllegalStateException(
                    "CSEventVisualiserUI is not initialised. " +
                            "Make sure to call initialise in Application#onCreate"
                )
            }
            return INSTANCE!!
        }

        /**
         * Call this when you are done using ev for that session.
         *
         * */
        internal fun release() {
            INSTANCE?.release()
            INSTANCE = null
        }
    }


}
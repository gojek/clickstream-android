package clickstream.lifecycle.internal

import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class CSLoggedInOnLifecycle(
    private val logger: CSLogger,
    private val isLoggedIn: suspend () -> Boolean,
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry()
) : Lifecycle by lifecycleRegistry {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        logger.debug { "CSLoggedInOnLifecycle#init" }
        subscribeToLoggedInState()
    }

    private fun subscribeToLoggedInState() {
        scope.launch {
            while (true) {
                delay(1000)
                toLifecycleState(isLoggedIn())
            }
        }
    }

    private fun toLifecycleState(isConnected: Boolean): Lifecycle.State {
        logger.debug { "CSLoggedInOnLifecycle#toLifecycleState : isConnected $isConnected" }

        return if (isConnected) {
            Lifecycle.State.Started
        } else {
            Lifecycle.State.Stopped.AndAborted
        }
    }
}
package clickstream.eventvisualiser.ui

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi

/***
 * Main entry point to Event visualiser.
 * @see [initialise] to initialise and [getInstance] to create and get an instance.
 * @see show to show ev window.
 *
 * */
@RequiresApi(Build.VERSION_CODES.O)
public class CSEventVisualiserUI private constructor() {

    /**
     * Call this to show ev window and start capturing events.
     *
     * */
    public fun show() {
        /*NoOp*/
    }

    /**
     * Call this to hide ev window and stop capturing events.
     *
     * */
    public fun close() {
        /*NoOp*/
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
                            CSEventVisualiserUI()
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
    }

}
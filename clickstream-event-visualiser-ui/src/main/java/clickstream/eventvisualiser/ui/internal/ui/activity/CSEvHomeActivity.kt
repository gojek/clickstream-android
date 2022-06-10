package clickstream.eventvisualiser.ui.internal.ui.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import clickstream.eventvisualiser.ui.CSEventVisualiserUI
import clickstream.eventvisualiser.ui.R
import clickstream.eventvisualiser.ui.internal.ui.viewmodel.CSEvViewModel

@RequiresApi(Build.VERSION_CODES.O)
internal class CSEvHomeActivity : AppCompatActivity() {

    private var viewModel: CSEvViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = CSEvViewModel.get(this)
        setContentView(R.layout.activity_cs_ev_home)
        viewModel?.getAllEventNames()
    }

    override fun finish() {
        CSEventVisualiserUI.getInstance().setVisibility(true)
        super.finish()
    }
}
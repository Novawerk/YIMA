package com.haodong.yimalaile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.di.create
import okio.Path.Companion.toPath

private const val DATA_STORE_FILE_NAME = "yimalaile.preferences_pb"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath() }
        )
        val component = AppComponent.create(dataStore)

        setContent {
            App(component)
        }
    }
}

package com.haodong.yimalaile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.di.create
import com.haodong.yimalaile.domain.export.AndroidReportExportService
import com.haodong.yimalaile.notifications.AndroidNotificationScheduler
import okio.Path.Companion.toPath

private const val DATA_STORE_FILE_NAME = "yimalaile.preferences_pb"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidNotificationScheduler.currentActivityHolder.set(this)

        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath() }
        )
        val scheduler = AndroidNotificationScheduler(applicationContext)
        val reportExportService = AndroidReportExportService(applicationContext)
        val component = AppComponent.create(dataStore, scheduler, reportExportService)

        setContent {
            App(component)
        }
    }

    override fun onDestroy() {
        AndroidNotificationScheduler.currentActivityHolder.compareAndSet(this, null)
        super.onDestroy()
    }

    @Deprecated("Using legacy permission API bridged to NotificationScheduler")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AndroidNotificationScheduler.onPermissionResult(requestCode, grantResults)
    }
}

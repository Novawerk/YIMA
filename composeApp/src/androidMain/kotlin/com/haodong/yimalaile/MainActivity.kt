package com.haodong.yimalaile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.haodong.yimalaile.data.AppDatabase
import com.haodong.yimalaile.data.AppSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppDatabase.init(DatabaseDriverFactory(this))
        AppSettings.init(KeyValueStoreFactory(this))
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}
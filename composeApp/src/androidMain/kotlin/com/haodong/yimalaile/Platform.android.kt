package com.haodong.yimalaile

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun dataStorePath(fileName: String): String {
    // On Android, the actual path is provided via context in MainActivity
    // This is a placeholder — the real path is set by the caller
    return fileName
}

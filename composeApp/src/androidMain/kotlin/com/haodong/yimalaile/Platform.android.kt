package com.haodong.yimalaile

actual fun dataStorePath(fileName: String): String {
    // On Android, the actual path is provided via context in MainActivity
    // This is a placeholder — the real path is set by the caller
    return fileName
}

package com.haodong.yimalaile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun dataStorePath(fileName: String): String

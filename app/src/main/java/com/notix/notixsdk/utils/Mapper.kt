package com.notix.notixsdk.utils

interface Mapper <F, T> {
    fun map(from: F): T
}
package com.notix.notixsdk.providers

import com.notix.notixsdk.di.SingletonComponent
import kotlinx.coroutines.launch

class NotixAudienceProvider {
    private val mainRepository = SingletonComponent.mainRepository
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()

    fun add(audience: String) = csIo.launch {
        mainRepository.manageAudience("add", audience)
    }

    fun delete(audience: String) = csIo.launch {
        mainRepository.manageAudience("delete", audience)
    }
}
package com.mr.mf_pd.application.view.splash

import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.UserRepository

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.operators.single.SingleCreate


import java.util.concurrent.TimeUnit

class SplashViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val delayTime = 2L

    fun start(): Single<Int> {
        return SingleCreate<Int> {
            it.onSuccess(userRepository.isLogin())
        }.delay(delayTime, TimeUnit.SECONDS)
    }

}
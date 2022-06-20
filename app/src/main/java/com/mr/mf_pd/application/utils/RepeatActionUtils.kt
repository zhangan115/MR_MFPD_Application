package com.mr.mf_pd.application.utils

import androidx.annotation.WorkerThread
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 执行重复的事件工具类
 * @author zhangan
 * @since 2022-06-14
 */
object RepeatActionUtils {

    /**
     * 执行重复事件
     * @param time 时间
     * @param unit 时间单位
     * @param callback 回调
     */
    fun execute(
        time: Long = 1,
        unit: TimeUnit = TimeUnit.SECONDS,
        callback: () -> (Unit),
    ): Disposable {
        return Observable.create { emitter: ObservableEmitter<Boolean?> ->
            try {
                callback()
                emitter.onNext(false)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                emitter.onComplete()
            }
        }.repeatWhen { objectObservable: Observable<Any?> ->
            objectObservable.delay(time, unit)
        }.subscribeOn(Schedulers.io()).subscribe()
    }
}
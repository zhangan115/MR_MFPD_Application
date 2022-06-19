package com.mr.mf_pd.application.view.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.ReadSettingCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

class FileDataViewModel(
    var dataRepository: DataRepository,
    val fileRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()
    lateinit var mCheckType: CheckType

    private var disposable: Disposable? = null

    private var ycDisposable: Disposable? = null
    private var realDisposable: Disposable? = null

    private var isStartReadYcData = false

    fun start(checkType: CheckType, file: File) {
        mCheckType = checkType
        dataRepository.setCheckType(checkType)

        CheckFileReadManager.get().addCallBack(CommandType.ReadYcData,ycCallback)

        fileRepository.openCheckFile(checkType, file, object : ReadSettingCallback {
            override fun onSettingBean(settingBean: SettingBean) {
                mCheckType.settingBean = settingBean
                val checkDir = file.parentFile
                if (checkDir != null) {
                    fileRepository.setCurrentClickFile(checkDir)
                }
                checkParamsBean = checkType.checkParams
                CheckFileReadManager.get().startReadData()
            }
        })
    }

    private val _toYcDataEvent = MutableLiveData<Event<ByteArray>>()
    val toYcDataEvent: LiveData<Event<ByteArray>> = _toYcDataEvent

    private val _toCleanDataEvent = MutableLiveData<Event<Unit>>()
    val toCleanDataEvent: LiveData<Event<Unit>> = _toCleanDataEvent

    private val ycCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (source.isEmpty()) {
                _toCleanDataEvent.postValue(Event(Unit))
                ycDisposable?.dispose()
                realDisposable?.dispose()
                isStartReadYcData = false
                CheckFileReadManager.get().startReadData()
            } else {
                _toYcDataEvent.postValue(Event(source))
            }
            if (!isStartReadYcData) {
                ycDisposable?.dispose()
                realDisposable?.dispose()
                ycDisposable = startReadYcData()
                realDisposable = startReadRealData()
                isStartReadYcData = true
            }
        }
    }

    private fun startReadYcData(
        time: Long = 1,
        unit: TimeUnit = TimeUnit.SECONDS,
    ): Disposable {
        return Observable.create { emitter: ObservableEmitter<Boolean> ->
            try {
                CheckFileReadManager.get().readYcDataFromFile()
                emitter.onNext(true)
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.repeatWhen { objectObservable: Observable<Any?> ->
            objectObservable.delay(time, unit)
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe()
    }

    private fun startReadRealData(
        time: Long = 20,
        unit: TimeUnit = TimeUnit.MILLISECONDS,
    ): Disposable {
        return Observable.create { emitter: ObservableEmitter<Boolean> ->
            try {
                CheckFileReadManager.get().readRealDataFromFile()
                CheckFileReadManager.get().readFlightDataFromFile()
                emitter.onNext(true)
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.repeatWhen { objectObservable: Observable<Any?> ->
            objectObservable.delay(time, unit)
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe()
    }


    override fun onCleared() {
        super.onCleared()
        CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData,ycCallback)
        disposable?.dispose()
        disposable = null
        ycDisposable?.dispose()
        ycDisposable = null
        realDisposable?.dispose()
        realDisposable = null
        fileRepository.releaseReadFile()
    }
}
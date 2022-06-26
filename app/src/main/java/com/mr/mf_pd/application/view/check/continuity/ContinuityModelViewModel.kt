package com.mr.mf_pd.application.view.check.continuity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import io.reactivex.disposables.Disposable
import java.io.File


class ContinuityModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var isSaveData: MutableLiveData<Boolean>? = null

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var showTimeView: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    var fzValueList: ArrayList<Float> = ArrayList()
    var yxValueList: ArrayList<Float> = ArrayList()
    var f1ValueList: ArrayList<Float> = ArrayList()
    var f2ValueList: ArrayList<Float> = ArrayList()

    var fzMinValue: MutableLiveData<String> = MutableLiveData()
    var limitValueStr: MutableLiveData<String> = MutableLiveData()
    var fzValue: MutableLiveData<String> = MutableLiveData()
    var yxMinValue: MutableLiveData<String> = MutableLiveData()
    var yxValue: MutableLiveData<String> = MutableLiveData()
    var f1MinValue: MutableLiveData<String> = MutableLiveData()
    var f1Value: MutableLiveData<String> = MutableLiveData()
    var f2MinValue: MutableLiveData<String> = MutableLiveData()
    var f2Value: MutableLiveData<String> = MutableLiveData()

    var text1: MutableLiveData<String> = MutableLiveData()
    var text2: MutableLiveData<String> = MutableLiveData()
    var text3: MutableLiveData<String> = MutableLiveData()
    var text4: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var saveDataStartTime: Long = 0
    var mTimeDisposable: Disposable? = null

    private val _toResetEvent = MutableLiveData<Event<Unit>>()
    val toResetEvent: LiveData<Event<Unit>> = _toResetEvent

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            showTimeView.postValue(true)
            val checkDataFileModel = filesRepository.getCheckFileModel()
            this.showTimeView.postValue(true)
            var startTime = 0L
            checkDataFileModel?.let {
                mTimeDisposable = RepeatActionUtils.execute {
                    it.dataTime?.let {
                        timeStr.postValue(DateUtil.timeFormat((it - startTime), "mm:ss"))
                        startTime += 1000L
                        if (startTime > it) {
                            resetFileRead()
                            startTime = 0
                        }
                    }
                }
            }
        } else {
            this.isSaveData = filesRepository.isSaveData()
            this.checkType = dataRepository.getCheckType()
            showTimeView.postValue(false)
        }
        updateTitle(checkType.settingBean)
    }

    /**
     * 重新从文件中读取
     */
    private fun resetFileRead() {
        _toResetEvent.postValue(Event(Unit))
        CheckFileReadManager.get().startReadData()
    }

    fun updateTitle(settingBean: SettingBean) {
        text1.postValue("有效值，" + settingBean.fzUnit)
        text2.postValue("峰值，" + settingBean.fzUnit)
        text3.postValue("F1(50Hz)，" + settingBean.fzUnit)
        text4.postValue("F2(100Hz)，" + settingBean.fzUnit)
    }

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile() {
        filesRepository.toCreateCheckFile(checkType)
    }

    fun startSaveData() {
        saveDataStartTime = System.currentTimeMillis()
        filesRepository.startSaveData()
        mTimeDisposable?.dispose()
        mTimeDisposable = RepeatActionUtils.execute {
            val time = System.currentTimeMillis() - saveDataStartTime
            timeStr.postValue(DateUtil.timeFormat(time, "mm:ss"))
        }
    }

    fun stopSaveData() {
        mTimeDisposable?.dispose()
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        fzValueList.clear()
        yxValueList.clear()
        f1ValueList.clear()
        f2ValueList.clear()
    }

    override fun onCleared() {
        super.onCleared()
        mTimeDisposable?.dispose()
    }
}
package com.mr.mf_pd.application.view.check.uhf

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import io.reactivex.disposables.Disposable

class CheckUHFViewModel(
    private val dataRepository: DataRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    private val disposableList = ArrayList<Disposable>()
    var settingBean: SettingBean? = null
    fun start(checkType: CheckType) {
        settingBean = settingRepository.getSettingData(checkType)
        dataRepository.setCheckType(checkType)
        dataRepository.switchPassageway(checkType.type)
//        readUHFValue(checkType)
    }

    private fun readUHFValue(checkType: CheckType) {
        //读取设置
        val command = CommandHelp.readSettingValue(checkType.type, 8)
        disposableList.add(
            SocketManager.getInstance()
                .sendData(command, CommandType.ReadSettingValue) { settingBytes ->
                    dealSettingValue(settingBytes)
                    val readYcCommand = CommandHelp.readYcValue(checkType.type)
                    //读取遥测
                    disposableList.add(
                        SocketManager.getInstance()
                            .sendData(readYcCommand, CommandType.ReadYcData) { ycBytes ->
                                dealYcValue(ycBytes)
//                                dataRepository.switchPassageway(checkType.type)
                            })
                })
    }

    private fun dealSettingValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)

        settingBean?.limitValue = valueList.last().toInt()
    }

    private fun dealYcValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        // TODO: 2/14/22
    }

    private fun splitBytesToValue(bytes: ByteArray): ArrayList<Float> {
        val valueList = ArrayList<Float>()
        if (bytes.size > 2) {
            val length = bytes[2].toInt()
            val source = ByteArray(length * 4)

            System.arraycopy(bytes, 3, source, 0, bytes.size - 5)
            for (i in 0 until (source.size / 4)) {
                val value = ByteArray(4)
                System.arraycopy(source, 4 * i, value, 0, 4)
                val f = ByteUtil.getFloat(value)
                valueList.add(f)
            }
            Log.d("zhangan", valueList.toString())
        }
        return valueList
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.closePassageway()
        disposableList.forEach { disposable ->
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposableList.clear()
    }
}
package com.mr.mf_pd.application.repository

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.opengl.`object`.PrPsCubeList
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

class DefaultDataRepository : DataRepository {

    companion object {
        var realDataMaxValue: MutableLiveData<Int> = MutableLiveData()
        var realDataMinValue: MutableLiveData<Int> = MutableLiveData()
    }

    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null

    override fun switchPassageway(passageway: Int,commandType: Int) {
        SocketManager.get().sendData(CommandHelp.switchPassageway(passageway,commandType))
    }

    override fun closePassageway() {
        SocketManager.get().sendData(CommandHelp.closePassageway())
    }

    override fun setCheckType(checkType: CheckType) {
        mCheckType = checkType
        checkParamsBean = mCheckType.checkParams.value
        realDataMaxValue.postValue(mCheckType.settingBean.maxValue)
        realDataMinValue.postValue(mCheckType.settingBean.minValue)
        checkType.checkParams.postValue(checkParamsBean)
    }

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    override fun readRepeatData(): Disposable {
        return SocketManager.get().sendRepeatData(CommandHelp.readYcValue(getCheckType().passageway),1)
    }

    override fun readContinuityYcData(): Disposable {
        return SocketManager.get().sendRepeatData(CommandHelp.readYcValue(getCheckType().passageway),100,TimeUnit.MILLISECONDS)
    }
}
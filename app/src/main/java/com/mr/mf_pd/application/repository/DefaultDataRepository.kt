package com.mr.mf_pd.application.repository

import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository

class DefaultDataRepository : DataRepository {

    var uhfModelBean: UHFModelBean? = null

    override fun getHufData(): UHFModelBean? {
        return uhfModelBean;
    }

}
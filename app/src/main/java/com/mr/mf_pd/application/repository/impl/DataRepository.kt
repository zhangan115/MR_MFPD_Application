package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.UHFModelBean

interface DataRepository {
    fun getHufData(): UHFModelBean?
}
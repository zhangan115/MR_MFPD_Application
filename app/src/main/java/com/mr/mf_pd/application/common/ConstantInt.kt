package com.mr.mf_pd.application.common

/**
 * int 型常量
 * Created by zhangan on 2017-06-21.
 */
interface ConstantInt {
    companion object {

        const val PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 10000
        const val VERSION = 1

        const val REQUEST_STATE_LOADING = 0
        const val REQUEST_STATE_DATA = 1
        const val REQUEST_STATE_EMPTY = 2
        const val REQUEST_STATE_ERROR = 3

    }
}
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

        const val LIMIT_VALUE_STEP = 10
        const val LIMIT_VALUE_MAX = 8192
        const val LIMIT_VALUE_MIN = 0

        const val ACTION_TAKE_PHOTO = 1000
        const val ACTION_CHOOSE_FILE = 2000

    }
}
package com.mr.mf_pd.application.common

object Constants {

    const val BYTES_PER_FLOAT = 4

    const val LINK_FAIL = -1
    const val LINK_SUCCESS = 1

    var indices = shortArrayOf(
        0, 3, 2, 0, 2, 1,
        0, 1, 5, 0, 5, 4,
        0, 7, 3, 0, 4, 7,
        6, 7, 4, 6, 7, 5,
        6, 3, 7, 6, 2, 3,
        6, 5, 1, 6, 1, 2
    )

    var indicesList = shortArrayOf()

    var PRPS_ROW = 50
    var PRPS_COLUMN = 100
}
package com.mr.mf_pd.application.common

object Constants {

    //       const val host = "172.16.40.45" //请求地址
    const val host = "192.168.88.2" //请求地址

    const val port = 8000 //端口

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

    var transparentColors = floatArrayOf(
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
        1f, 1f, 1f, 0f,
    )

    var whiteColors = floatArrayOf(
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
        1f, 1f, 1f, 1f,
    )

    var yellowColors = floatArrayOf(
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,
    )

    var blueColors = floatArrayOf(
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,
    )

    var redColors = floatArrayOf(
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
    )

    var greenColors = floatArrayOf(
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
    )

    var indicesList = shortArrayOf()

    const val PRPS_ROW = 50
    const val PRPS_COLUMN = 100

    const val KEY_UNIT = "key_unit"
    const val KEY_X_TEXT = "key_x_text"
    const val KEY_Y_TEXT = "key_y_text"
}
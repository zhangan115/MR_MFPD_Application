package com.mr.mf_pd.application.common

object Constants {

    const val BYTES_PER_FLOAT = 4

    const val LINK_FAIL = -1
    const val LINK_SUCCESS = 1

    const val PRPS_SPACE = 0.15f

    const val UPDATE_SETTING = "update_setting"

    const val CHANNEL_ID = "Lower_Device_Power"

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
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
        0f, 0f, 1f, 0.7f,
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
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
        0f, 1f, 0f, 0.7f,
    )

    var indicesList = shortArrayOf()

    const val PRPS_ROW = 50
    const val PRPS_COLUMN = 100
    const val FLIGHT_COLUMN = 20000

    const val KEY_UNIT = "key_unit"
    const val KEY_X_TEXT = "key_x_text"
    const val KEY_Y_TEXT = "key_y_text"
    const val KEY_Z_TEXT = "key_z_text"

    val PHASE_MODEL_LIST = listOf("内同步", "无线同步", "智能同步")
    val BAND_DETECTION_LIST = listOf("全通", "高通", "低通")
}
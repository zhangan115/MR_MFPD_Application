package com.mr.mf_pd.application.common

/**
 * str 常量
 */
interface ConstantStr {

    companion object {

        const val MR_FILE = "MR"
        const val cancelVersion = "cancelVersion" //取消的版本升级
        const val USER_CONFIG_FILE = "user_config"
        const val APP_HOST = "app_host"
        const val APP_PORT = "app_port"
        const val NEED_LOGIN = "need_login"
        const val PROMISE_READ = "promise_read"
        const val CURRENT_VERSION = "当前版本：V1.0"
        const val USER_LOCATION = "user_location"

        const val CHECK_FILE_DIR = "check_file_dir"//配置的默认保存位置
        const val CHECK_FILE_CONFIG = "config.json"//配置文件
        const val CHECK_FILE_SETTING = "setting.json"//设置文件
        const val CHECK_REAL_DATA = "check_real.mr"//存放实时数据文件
        const val CHECK_YC_FILE_NAME = "check_telemeter.mr"//存放遥测文件
        const val CHECK_FLIGHT_FILE_NAME = "check_flight.mr"//存放飞行数据文件
        const val CHECK_PULSE_FILE_NAME = "check_pulse.mr"//存放脉冲数据文件
        const val CHECK_FD_FILE_NAME = "check_discharge.mr"//存放放电数据文件


        /**
         * 登录账户的信息key值
         */
        const val PROFILE_ACCOUNT = "account"

        /**
         * 消息临时存储
         */
        const val NEWS_COUNT = "news_count"

        /**
         * 设置ip信息
         */
        const val IP_INFO = "ip_info"

        /**
         * 用户数据保存文件
         */
        const val USER = "user"

        const val SETTING_DATA = "setting_data"

        const val SETTING_UHF = "setting_key_uhf"
        const val SETTING_TEV = "setting_key_tev"
        const val SETTING_HF = "setting_key_hf"
        const val SETTING_AE = "setting_key_ae"
        const val SETTING_AA = "setting_key_aa"

        /**
         * user store
         */
        const val USER_DATA = "user_data"
        const val DEVICE_LIST = "device_list"
        const val SUBSTATION_LIST = "substation_list"
        const val USER_GUIDE = "is_guide"
        const val APP_DATA = "app_data"

        /**
         * user信息key值
         */
        const val USER_INFO = "user_info"
        const val USER_VERSION = "user_version"
        const val DEVICE = "device"
        const val USER_BEAN = "user_bean"

        /**
         * Bundle Key
         */
        const val KEY_BUNDLE_STR = "key_str"
        const val KEY_BUNDLE_STR_1 = "key_str_1"
        const val KEY_BUNDLE_STR_2 = "key_str_2"
        const val KEY_BUNDLE_INT = "key_int"
        const val KEY_BUNDLE_INT_1 = "key_int_1"
        const val KEY_BUNDLE_INT_2 = "key_int_2"
        const val KEY_BUNDLE_LONG = "key_long"
        const val KEY_BUNDLE_LONG_1 = "key_long_1"
        const val KEY_BUNDLE_LONG_2 = "key_long_2"
        const val KEY_BUNDLE_DOUBLE = "key_double"
        const val KEY_BUNDLE_DOUBLE_1 = "key_double_1"

        const val KEY_BUNDLE_OBJECT = "key_object"
        const val KEY_BUNDLE_OBJECT_1 = "key_object_1"
        const val KEY_BUNDLE_OBJECT_2 = "key_object_2"
        const val KEY_BUNDLE_LIST = "key_list"
        const val KEY_BUNDLE_LIST_1 = "key_list_1"
        const val KEY_BUNDLE_LIST_2 = "key_list_2"
        const val KEY_BUNDLE_TITLE = "key_title"
        const val KEY_BUNDLE_BOOLEAN = "key_boolean"
        const val KEY_BUNDLE_BOOLEAN_1 = "key_boolean_1"
        const val KEY_BUNDLE_BOOLEAN_2 = "key_boolean_2"
        const val USER_NAME = "user_name"
        const val USER_PASS = "user_pass"
        const val COOKIE_DOMAIN = "cookie_domain"
        const val COOKIE_NAME = "cookie_name"
        const val COOKIE_VALUE = "cookie_value"
        const val KEY_URL = "key_url"
        const val KEY_ID = "key_id"
        const val KEY_TITLE = "key_title"
        const val SUCCESS = 0
        const val TIMEOUT = 1001
        const val ERROR_PASS = -101
        const val CLOSE_WORK = "close_work"
        const val SEND_DATA = "send_data"
        const val SEND_CHART_DATA = "send_chart_data"
    }
}
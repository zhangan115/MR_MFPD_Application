package com.mr.mf_pd.application.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public class NumberUtils {

    public static boolean isNumber(String str) {
        if (TextUtils.isEmpty(str)) return false;
        return str.matches("-?[0-9]+.？[0-9]*");
    }

    @Nullable
    public static Float getFloat(String str) {
        Float value = null;
        try {
            value = Float.parseFloat(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}

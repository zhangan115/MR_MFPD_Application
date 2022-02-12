package com.mr.mf_pd.application.utils;

import java.text.DecimalFormat;

public class StringUtils {

    public static String floatValueToStr(Float value) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        return decimalFormat.format(value);
    }
}

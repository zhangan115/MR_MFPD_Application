package com.mr.mf_pd.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)//保留
@Target({ElementType.TYPE})//作用目标
public @interface Lance {
}

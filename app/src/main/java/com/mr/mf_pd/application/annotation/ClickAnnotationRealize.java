package com.mr.mf_pd.application.annotation;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 注解实现类
 *
 * @author zhangan
 */
public class ClickAnnotationRealize {

    private static Long START_TIME;
    private static final Long TIME = 500L;

    /**
     * 使用注解进行 View 的单击 或者长按操作
     *
     * @param activity activity
     */
    public static void Bind(Activity activity) {
        START_TIME = -1L;
        Class<Activity> clazz = (Class<Activity>) activity.getClass();
        for (int i = 0; i < clazz.getDeclaredMethods().length; i++) {
            Method method = clazz.getDeclaredMethods()[i];
            if (method.isAnnotationPresent(OnClick.class)) {
                OnClick onClick = method.getAnnotation(OnClick.class);
                assert onClick != null;
                activity.findViewById(onClick.value()).setOnClickListener(v -> {
                    try {
                        if (System.currentTimeMillis() - START_TIME > TIME) {
                            method.invoke(activity, v);
                        }
                        START_TIME = System.currentTimeMillis();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                OnLongClick onLongClick = method.getAnnotation(OnLongClick.class);
                assert onLongClick != null;
                activity.findViewById(onLongClick.value()).setOnLongClickListener(v -> {
                    try {
                        if (System.currentTimeMillis() - START_TIME > TIME) {
                            method.invoke(activity, v);
                        }
                        START_TIME = System.currentTimeMillis();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return onLongClick.isEnable();
                });
            }
        }
    }

    /**
     * 使用注解进行 View 的单击 或者长按操作
     *
     * @param fragment fragment
     */
    public static void Bind(Fragment fragment) {
        Class<Fragment> clazz = (Class<Fragment>) fragment.getClass();
        for (int i = 0; i < clazz.getDeclaredMethods().length; i++) {
            if (fragment.getView() == null) {
                continue;
            }
            Method method = clazz.getDeclaredMethods()[i];
            if (method.isAnnotationPresent(OnClick.class)) {
                OnClick onClick = method.getAnnotation(OnClick.class);
                assert onClick != null;
                fragment.getView().findViewById(onClick.value()).setOnClickListener(v -> {
                    try {
                        if (System.currentTimeMillis() - START_TIME > TIME) {
                            method.invoke(fragment, v);
                        }
                        START_TIME = System.currentTimeMillis();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                OnLongClick onLongClick = method.getAnnotation(OnLongClick.class);
                assert onLongClick != null;
                fragment.getView().findViewById(onLongClick.value()).setOnLongClickListener(v -> {
                    try {
                        if (System.currentTimeMillis() - START_TIME > TIME) {
                            method.invoke(fragment, v);
                        }
                        START_TIME = System.currentTimeMillis();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return onLongClick.isEnable();
                });
            }
        }
    }
}

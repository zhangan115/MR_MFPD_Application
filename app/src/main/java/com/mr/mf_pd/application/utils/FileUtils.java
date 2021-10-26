package com.mr.mf_pd.application.utils;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.mr.mf_pd.application.common.ConstantStr;
import com.mr.mf_pd.application.view.file.filter.FileFilter;
import com.mr.mf_pd.application.view.file.model.CheckConfigModel;
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.objectbox.android.AndroidScheduler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FileUtils {

    public interface FilterCallback {

        void onResult(List<CheckDataFileModel> list);

    }

    public static Disposable getFileList(File directory, @Nullable FileTypeUtils.CheckType checkType, FilterCallback callback) {
        Observable<List<CheckDataFileModel>> observable = Observable.create((ObservableOnSubscribe<List<CheckDataFileModel>>) emitter -> {
            List<CheckDataFileModel> checkDataFileModels = new ArrayList<>();
            File[] files = directory.listFiles();
            try {
                if (files != null) {
                    //需要展示的文件
                    List<File> filterFiles = new ArrayList<>();
                    for (File file : files) {
                        if (file.getName().startsWith(FileTypeUtils.getCheckTypeStr(checkType))) {
                            filterFiles.add(file);
                        }
                    }
                    for (int i = 0; i < filterFiles.size(); i++) {
                        File file = filterFiles.get(i);
                        if (file.isDirectory()) {
                            CheckDataFileModel model = new CheckDataFileModel();
                            model.setFile(file);
                            model.setSelect(false);
                            FileTypeUtils.CheckType ct = FileTypeUtils.getCheckTypeFromFile(file);
                            if (ct != null) {
                                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                                    if (listFile.getName().endsWith(".png")
                                            || listFile.getName().endsWith(".jpg")) {
                                        model.setHasPhoto(true);
                                    }
                                    if (listFile.getName().equals(ConstantStr.CHECK_FILE_CONFIG)) {
                                        String str = readStrFromFile(listFile);
                                        if (!TextUtils.isEmpty(str)) {
                                            CheckConfigModel checkConfigModel = new Gson().fromJson(str, CheckConfigModel.class);
                                            model.setColor(checkConfigModel.getColor());
                                            model.setMarks(checkConfigModel.getMarks());
                                        }
                                    }
                                }
                                model.setCheckFile(true);
                                model.setCheckType(ct);
                            } else {
                                model.setCheckFile(false);
                            }
                            checkDataFileModels.add(model);
                        }
                    }
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                emitter.onNext(checkDataFileModels);
                emitter.onComplete();
            }
            emitter.onComplete();
        });
        return observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(callback::onResult);
    }

    @Nullable
    public static File getParentOrNull(File file) {
        if (file.getParent() == null) {
            return null;
        }

        return file.getParentFile();
    }

    public static boolean isParent(File maybeChild, File possibleParent) {
        if (!possibleParent.exists() || !possibleParent.isDirectory()) {
            return false;
        }

        File child = maybeChild;
        while (child != null) {
            if (child.equals(possibleParent)) {
                return true;
            }
            child = child.getParentFile();
        }

        return false;
    }

    /**
     * 从文件中读取数据
     *
     * @param file 文件
     * @return 数据
     * @throws IOException IO异常
     */
    @Nullable
    public static String readStrFromFile(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = new FileInputStream(file);
        StringBuilder sb = new StringBuilder();
        int size = 0;
        byte[] bytes = new byte[1024];
        while ((size = fis.read(bytes)) != -1) {
            sb.append(new String(bytes, 0, size));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * 将数据写入到文件中
     *
     * @param str  数据
     * @param file 文件
     * @throws IOException IO异常
     */
    public static void writeStr2File(String str, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(str.getBytes(StandardCharsets.UTF_8));
        fos.flush();
        fos.close();
    }
}

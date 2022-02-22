package com.mr.mf_pd.application.utils;


import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.mr.mf_pd.application.common.ConstantStr;
import com.mr.mf_pd.application.view.file.model.CheckConfigModel;
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FileUtils {

    public interface FilterCallback {

        void onResult(List<CheckDataFileModel> list);

    }

    public static void copyFile(File source, File targetFile) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        if (!targetFile.exists()) {
            targetFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(targetFile, true);
        byte[] buf = new byte[1024 * 4];
        int size;
        while ((size = fis.read(buf)) != -1) {
            fos.write(buf, 0, size);
            fos.flush();
        }
    }

    public static Disposable getFileList(File directory, @Nullable FileTypeUtils.FileType checkType, FilterCallback callback) {
        Observable<List<CheckDataFileModel>> observable = Observable.create((ObservableOnSubscribe<List<CheckDataFileModel>>) emitter -> {
            List<CheckDataFileModel> checkDataFileModels = new ArrayList<>();
            File[] files = directory.listFiles();
            try {
                if (files != null) {
                    //需要展示的文件
                    List<File> filterFiles = new ArrayList<>();
                    for (File file : files) {
                        String startName = FileTypeUtils.getCheckTypeStr(checkType);
                        if (file.getName().startsWith(startName)) {
                            filterFiles.add(file);
                        } else if (file.isDirectory() && !file.getName().startsWith(".")) {
                            filterFiles.add(file);
                        }
                    }
                    for (int i = 0; i < filterFiles.size(); i++) {
                        File file = filterFiles.get(i);
                        if (file.isDirectory()) {
                            CheckDataFileModel model = new CheckDataFileModel();
                            model.setFile(file);
                            model.setSelect(false);
                            FileTypeUtils.FileType ct = FileTypeUtils.getCheckTypeFromFile(file);
                            if (ct != null) {
                                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                                    if (listFile.getName().endsWith(".png")
                                            || listFile.getName().endsWith(".jpg")
                                            || listFile.getName().endsWith(".jpeg")) {
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
                                model.setFileType(ct);
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
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(str.getBytes(StandardCharsets.UTF_8));
        fos.flush();
        fos.close();
    }

    /**
     * @param source 源数据
     * @param file   文件
     * @throws IOException 异常
     */
    public static void writeByteArray2File(byte[] source, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(source);
        fos.flush();
        fos.close();
    }

    /**
     * 删除文件集合(异步)
     *
     * @param files    文件集合
     * @param listener 回调
     * @return 订阅
     */
    public static Disposable deleteFiles(List<File> files, FileActionListener listener) {
        Observable<Boolean> observable = Observable.create(emitter -> {
            try {
                boolean isSuccess = true;
                for (int i = 0; i < files.size(); i++) {
                    if (!deleteFile(files.get(i))) {
                        isSuccess = false;
                        break;
                    }
                }
                emitter.onNext(isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        });
        return observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(isSuccess -> {
                    if (isSuccess) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }, throwable -> listener.onFail());
    }

    /**
     * 删除掉文件或者文件夹
     *
     * @param dirFile 文件
     * @return 删除是否成功
     */
    public static boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }
        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            File[] files = dirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFile(file);
                }
            }
        }
        return dirFile.delete();
    }


    public interface FileActionListener {

        void onSuccess();

        void onFail();
    }

    public static Disposable cutFiles(File target, ArrayList<File> files, FileActionListener listener) {
        Observable<Boolean> observable = Observable.create(emitter -> {
            try {
                boolean isSuccess = cutFiles(target, files);
                emitter.onNext(isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            } finally {
                emitter.onComplete();
            }
        });
        return observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(isSuccess -> {
                    if (isSuccess) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }, throwable -> listener.onFail());
    }

    public static boolean cutFiles(File target, ArrayList<File> files) {
        for (int i = 0; i < files.size(); i++) {
            if (isInFile(target, files.get(i))) {
                return false;
            }
        }
        for (int i = 0; i < files.size(); i++) {
            copyFileTiFile(target, files.get(i));
        }
        return true;
    }

    private static boolean isInFile(File target, File cutFile) {
        if (cutFile.isDirectory()) {
            File[] files = cutFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && target.getAbsolutePath().equals(file.getAbsolutePath())) {
                        return true;
                    } else if (file.isDirectory()) {
                        if (isInFile(target, file)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void copyFileTiFile(File target, File file) {
        if (file.isDirectory()) {
            File newFile = new File(target, file.getName());
            if (newFile.mkdir()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File value : files) {
                        copyFileTiFile(newFile, value);
                    }
                }
            }
        } else {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                File f = new File(target, file.getName());
                if (f.createNewFile()) {
                    fos = new FileOutputStream(f, true);
                    byte[] bytes = new byte[1024 * 4];
                    int size;
                    while ((size = fis.read(bytes)) != -1) {
                        fos.write(bytes, 0, size);
                        fos.flush();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

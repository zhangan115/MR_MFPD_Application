package com.mr.mf_pd.application.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ReadOrWriteFile {

    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sbf = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }

    public static boolean writeContentToFile(String fileName, String text) {
        File file = new File(fileName);
        boolean isSave = true;
        PrintWriter bw = null;
        try {
            if (!file.exists()) {
                isSave = file.createNewFile();
            }
            bw = new PrintWriter(file);
            bw.print(text);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            isSave = false;
        } finally {
            if (bw != null)
                bw.close();
        }
        return isSave;
    }
}

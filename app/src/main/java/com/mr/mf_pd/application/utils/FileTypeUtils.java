package com.mr.mf_pd.application.utils;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;


import com.mr.mf_pd.application.R;
import com.mr.mf_pd.application.common.CheckType;
import com.mr.mf_pd.application.common.ConstantStr;
import com.mr.mf_pd.application.common.Constants;
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class FileTypeUtils {

    @Nullable
    public static FileType getCheckTypeFromFile(@Nullable String typeName) {
        if (TextUtils.isEmpty(typeName)) {
            return null;
        }
        assert typeName != null;
        if (typeName.equals("ae")) {
            return FileType.AE;
        }
        if (typeName.equals("aa")) {
            return FileType.AA;
        }
        if (typeName.equals("hf")) {
            return FileType.HF;
        }
        if (typeName.equals("tev")) {
            return FileType.TEV;
        }
        if (typeName.equals("uhf")) {
            return FileType.UHF;
        }
        return null;
    }

    @Nullable
    public static CheckType getCheckType(FileType fileType) {
        switch (fileType) {
            case UHF:
                return CheckType.UHF;
            case HF:
                return CheckType.HF;
            case AE:
                return CheckType.AE;
            case TEV:
                return CheckType.TEV;
            case AA:
                return CheckType.AA;
            default:
                return null;
        }
    }

    @Nullable
    public static String getFileTypeName(FileType fileType) {
        switch (fileType) {
            case UHF:
                return "uhf";
            case HF:
                return "hf";
            case AE:
                return "ae";
            case TEV:
                return "tev";
            case AA:
                return "aa";
            default:
                return null;
        }
    }

    public static FileType getFileType(int type) {
        return FileType.values()[type];
    }

    public enum FileType {
        DIRECTORY(R.mipmap.data_icon_file, R.string.type_directory),
        UHF(R.mipmap.detect_icon_uhf, R.string.type_uhf),
        AE(R.mipmap.detect_icon_ac, R.string.type_ac),
        AA(R.mipmap.detect_icon_aa, R.string.type_ac),
        TEV(R.mipmap.detect_icon_tev, R.string.type_tev),
        HF(R.mipmap.detect_icon_hf, R.string.type_hf);

        private final int icon;
        private final int description;
        private final String[] extensions;

        FileType(int icon, int description, String... extensions) {
            this.icon = icon;
            this.description = description;
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public int getIcon() {
            return icon;
        }

        public int getDescription() {
            return description;
        }
    }

    private static final Map<String, FileType> fileTypeExtensions = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(CheckDataFileModel file) {
        if (file.getFileType()==null){
            return FileType.DIRECTORY;
        }
        return file.getFileType();
    }

    private static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }
}

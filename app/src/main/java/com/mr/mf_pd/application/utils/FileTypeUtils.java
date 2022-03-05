package com.mr.mf_pd.application.utils;

import android.webkit.MimeTypeMap;


import com.mr.mf_pd.application.R;
import com.mr.mf_pd.application.common.CheckType;
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class FileTypeUtils {


    public static String getCheckTypeStr(@Nullable FileType fileType) {
        if (fileType == FileType.AE) {
            return ".check_ae";
        } else if (fileType == FileType.HF) {
            return ".check_hf";
        } else if (fileType == FileType.UHF) {
            return ".check_uhf";
        } else if (fileType == FileType.TEV) {
            return ".check_tev";
        }
        return "";
    }

    @Nullable
    public static FileType getCheckTypeFromFile(File file) {
        if (file.getName().startsWith(".check_ae")) {
            return FileType.AE;
        }
        if (file.getName().startsWith(".check_hf")) {
            return FileType.HF;
        }
        if (file.getName().startsWith(".check_tev")) {
            return FileType.TEV;
        }
        if (file.getName().startsWith(".check_uhf")) {
            return FileType.UHF;
        }
        return null;
    }

    @Nullable
    public static CheckType getCheckType(FileType fileType){
        switch (fileType){
            case UHF:
                return CheckType.UHF;
            case HF:
                return CheckType.HF;
            case AE:
                return CheckType.AE;
            case TEV:
                return CheckType.TEV;
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

    private static Map<String, FileType> fileTypeExtensions = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(CheckDataFileModel file) {
        if (Objects.requireNonNull(file.getFile()).getName().startsWith(".check_ae")) {
            return FileType.AE;
        }
        if (file.getFile().getName().startsWith(".check_hf")) {
            return FileType.HF;
        }
        if (file.getFile().getName().startsWith(".check_tev")) {
            return FileType.TEV;
        }
        if (file.getFile().getName().startsWith(".check_uhf")) {
            return FileType.UHF;
        }
        return FileType.DIRECTORY;
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

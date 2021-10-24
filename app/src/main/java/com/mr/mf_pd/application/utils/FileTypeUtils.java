package com.mr.mf_pd.application.utils;

import android.webkit.MimeTypeMap;


import com.mr.mf_pd.application.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class FileTypeUtils {

    public enum FileType {
        DIRECTORY(R.mipmap.data_icon_file, R.string.type_directory),
        UHF(R.drawable.ic_document_box, R.string.type_uhf),
        AC(R.drawable.ic_document_box, R.string.type_ac),
        TEV(R.drawable.ic_document_box, R.string.type_tev),
        HF(R.drawable.ic_document_box, R.string.type_hf);
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

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }

        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
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

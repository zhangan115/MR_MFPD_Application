package com.mr.mf_pd.application.view.file.filter;

import java.io.File;
import java.io.Serializable;

public interface FileFilter extends Serializable {
    boolean accept(File pathname);
}

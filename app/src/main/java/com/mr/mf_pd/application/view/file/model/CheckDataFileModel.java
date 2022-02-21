package com.mr.mf_pd.application.view.file.model;

import com.mr.mf_pd.application.utils.FileTypeUtils;

import java.io.File;

public class CheckDataFileModel {

    private boolean isCheckFile = false;//是否是检测文件
    private File file;//当前的检测文件
    private String marks;//文件描述
    private boolean hasPhoto;//是否存在图片
    private int color = -1;//文件标识颜色
    private FileTypeUtils.FileType fileType;//检测文件类型
    private boolean isSelect;//是否被选中
    private boolean toChooseModel = false;

    public boolean isCheckFile() {
        return isCheckFile;
    }

    public void setCheckFile(boolean checkFile) {
        isCheckFile = checkFile;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public boolean isHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public FileTypeUtils.FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileTypeUtils.FileType fileType) {
        this.fileType = fileType;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isToChooseModel() {
        return toChooseModel;
    }

    public void setToChooseModel(boolean toChooseModel) {
        this.toChooseModel = toChooseModel;
    }
}

package com.mr.mf_pd.application.view.opengl;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class TextBitmap {

    private int textureObjectIds;
    private Rect rect;
    private Bitmap bitmap;

    public TextBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public TextBitmap() {
    }

    public TextBitmap(Rect rect, Bitmap bitmap) {
        this.rect = rect;
        this.bitmap = bitmap;
    }

    public int getTextureObjectIds() {
        return textureObjectIds;
    }

    public void setTextureObjectIds(int textureObjectIds) {
        this.textureObjectIds = textureObjectIds;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

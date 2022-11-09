package com.smartlife.smartcart.classes;


import com.smartlife.smartcart.R;

/* renamed from: cn.chaohi.shopping.managers.sound.SoundsEnum */
public enum SoundsEnum {

    SOUNDS_SCAN_SUCCESS(33, 2, R.raw.scan_success);

    
    private int code;
    private int resourceId;
    private int type;

    private SoundsEnum(int code2, int type2, int resourceId2) {
        this.code = code2;
        this.type = type2;
        this.resourceId = resourceId2;
    }

    public int getType() {
        return this.type;
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public int getCode() {
        return this.code;
    }
}

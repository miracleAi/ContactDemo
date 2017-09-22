package com.example.zhulinping.contactdemo.contactdisplay.contactdata.model;

import android.text.TextUtils;

import com.example.zhulinping.contactdemo.contactdisplay.view.slidebar.Indexable;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactInfo extends ContactDbInfo implements Indexable {
    public static final int IS_RECENT = 1;
    public static final String FAVOURITE_FLAG_TXT = "FAVOURITE";
    public static final String RECENT_FLAG_TXT = "RECENT";
    public static final String FAVOURITE_FLAG = "$";
    public static final String RECENT_FLAG = "%";
    //是否是最近联系热 1-是 0-不是
    public int isRecentContact;
    public String firstLetter;
    public String indexFlag;
    public long lastContactTime;
    public String lookupUri;
    public String note;
    public String phone;
    @Override
    public String getIndex() {
        if (indexFlag.equals(FAVOURITE_FLAG)) {
            return "☆";
        } else if (indexFlag.equals(RECENT_FLAG)) {
            return "*";
        } else {
            return indexFlag;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContactInfo)) {
            return false;
        }
        ContactInfo target = (ContactInfo) o;
        if (target.contactName == null) {
            return false;
        }
        return TextUtils.equals(this.contactName, target.contactName);
    }
}

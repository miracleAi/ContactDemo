package com.example.zhulinping.contactdemo.contactdata.model;

import android.support.annotation.NonNull;

import com.example.zhulinping.contactdemo.view.slidebar.Indexable;

import java.util.List;

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
    private int isRecentContact;
    private String firstLetter;
    private String indexFlag;

    public String getIndexFlag() {
        return indexFlag;
    }

    public void setIndexFlag(String indexFlag) {
        this.indexFlag = indexFlag;
    }

    public long getLastContactTime() {
        return lastContactTime;
    }

    public void setLastContactTime(long lastContactTime) {
        this.lastContactTime = lastContactTime;
    }

    private long lastContactTime;

    public int getIsRecentContact() {
        return isRecentContact;
    }

    public void setIsRecentContact(int isRecentContact) {
        this.isRecentContact = isRecentContact;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    @Override
    public String getIndex() {
        if (indexFlag.equals(FAVOURITE_FLAG)) {
            return "⭐";
        } else if (indexFlag.equals(RECENT_FLAG)) {
            return "*";
        } else {
            return indexFlag;
        }
    }
}

package com.example.zhulinping.contactdemo.contactdata.model;

import java.util.List;

/**
 * Created by zhulinping on 17/7/30.
 */

public class ContactDbInfo {
    public static final int IS_FAVOURITE = 1;
    private String contactName;
    private String sortKeyName;
    private String photo;
    private String photoThumbnail;
    private List<String> phoneNumList;
    private List<String> emailList;
    //是否已收藏 ：1-收藏 0-未收藏
    private int isFavourite;
    private long lastContactTime;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getSortKeyName() {
        return sortKeyName;
    }

    public void setSortKeyName(String sortKeyName) {
        this.sortKeyName = sortKeyName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhotoThumbnail() {
        return photoThumbnail;
    }

    public void setPhotoThumbnail(String photoThumbnail) {
        this.photoThumbnail = photoThumbnail;
    }

    public List<String> getPhoneNumList() {
        return phoneNumList;
    }

    public void setPhoneNumList(List<String> phoneNumList) {
        this.phoneNumList = phoneNumList;
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

    public int getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(int isFavourite) {
        this.isFavourite = isFavourite;
    }

    public long getLastContactTime() {
        return lastContactTime;
    }

    public void setLastContactTime(long lastContactTime) {
        this.lastContactTime = lastContactTime;
    }
}

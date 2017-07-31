package com.example.zhulinping.contactdemo.contactdata.model;

import java.util.List;

/**
 * Created by zhulinping on 17/7/30.
 */

public class ContactDbInfo {
    public static final int IS_FAVOURITE = 1;
    public String contactId;
    private String contactName;
    private String photo;
    private String photoThumbnail;
    private List<String> phoneNumList;
    private List<String> emailList;
    //是否已收藏 ：1-收藏 0-未收藏
    private int isFavourite;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
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
}

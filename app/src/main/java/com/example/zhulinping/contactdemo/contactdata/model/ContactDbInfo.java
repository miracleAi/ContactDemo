package com.example.zhulinping.contactdemo.contactdata.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhulinping on 17/7/30.
 */

public class ContactDbInfo {
    public static final int IS_FAVOURITE = 1;
    public long contactId;
    public String contactName;
    public String photo;
    public String photoThumbnail;
    public List<String> phoneNumList = new ArrayList<>();
    public List<String> emailList = new ArrayList<>();
    //是否已收藏 ：1-收藏 0-未收藏
    public int isFavourite;
}

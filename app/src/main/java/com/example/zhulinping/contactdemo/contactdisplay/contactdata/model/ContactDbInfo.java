package com.example.zhulinping.contactdemo.contactdisplay.contactdata.model;

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
    public static class TypeInfo{
        public int type;
        public String value;
    }
    public List<TypeInfo> phoneNumList = new ArrayList<>();
    public List<TypeInfo> emailList = new ArrayList<>();
    //是否已收藏 ：1-收藏 0-未收藏
    public int isFavourite;
}

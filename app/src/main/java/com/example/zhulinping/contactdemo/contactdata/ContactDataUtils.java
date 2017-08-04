package com.example.zhulinping.contactdemo.contactdata;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.ecity.android.tinypinyin.Pinyin;
import com.example.zhulinping.contactdemo.contactdata.model.ContactDbInfo;
import com.example.zhulinping.contactdemo.contactdata.model.ContactInfo;
import com.example.zhulinping.contactdemo.diaplay.ContactContact;
import com.example.zhulinping.contactdemo.utils.CnToSpell;
import com.example.zhulinping.contactdemo.utils.SortByFirstComparator;
import com.example.zhulinping.contactdemo.utils.SortByTimeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhulinping on 2017/7/28.
 */

public class ContactDataUtils {
    private static String TAG = "test.zlp";

    /**
     * 获取包含联系人详细信息的表
     */
    public static List<ContactInfo> getContactDetailList(Context context) {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = {ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.STARRED,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER};
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection,
                    null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
            if (null == cursor) {
                return null;
            }
            ArrayList<ContactInfo> list = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                ContactInfo contact = new ContactInfo();
                contact.contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.lookupUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                contact.contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.photo = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                contact.photoThumbnail = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                contact.isFavourite = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)));
                contact.firstLetter = getFirstLetter(contact.contactName);
                contact.isRecentContact = 0;
                if (contact.isFavourite == ContactInfo.IS_FAVOURITE) {
                    contact.indexFlag = ContactInfo.FAVOURITE_FLAG;
                } else {
                    contact.indexFlag = contact.firstLetter;
                }
                list.add(contact);
            }
            return list;

        } catch (Exception e) {
                Log.e(TAG, "", e);
            return null;
        } finally {
            cursor.close();
        }
    }

    //查询联系人电话、邮件等信息
    public static void fillContactInfos(Context context, List<ContactInfo> source) {
        if (source == null) {
            return;
        }

        final String[] projection = new String[]{ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1};
        final String TYPE_PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        final String TYPE_EMAIL = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
        final String TYPE_NOTE = ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE;
        String select = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?" + " and (" + ContactsContract.Data.MIMETYPE + " = '" + TYPE_EMAIL + "' or "
                + ContactsContract.Data.MIMETYPE + "='" + TYPE_NOTE + "' or "
                + ContactsContract.Data.MIMETYPE + "='" + TYPE_PHONE + "')";

        ContentResolver contentResolver = context.getContentResolver();
        for (ContactInfo bean : source) {
            if (bean.contactId < 0) {
                continue;
            }

            bean.phoneNumList.clear();
            bean.emailList.clear();

            //电话
            Cursor phoneCursor = null;
            try {
                phoneCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI
                        , projection
                        , select
                        , new String[]{String.valueOf(bean.contactId)}
                        , null);
                if (phoneCursor == null) {
                    continue;
                }
                phoneCursor.moveToPosition(-1);
                while (phoneCursor.moveToNext()) {
                    String type = phoneCursor.getString(0);
                    String value = phoneCursor.getString(1);
                    switch (type) {
                        case TYPE_EMAIL: {
                            bean.emailList.add(value);
                            break;
                        }
                        case TYPE_NOTE: {
                            bean.note = value;
                            break;
                        }
                        case TYPE_PHONE: {
                            bean.phoneNumList.add(value);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                    Log.e(TAG, "fillContactInfos: ", e);
            } finally {
               phoneCursor.close();
            }
        }
    }

    //获取通话记录
    @SuppressLint("MissingPermission")
    public static List<ContactInfo> getRecentCalls(Context context) {
        ArrayList<ContactInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.CACHED_NAME,  //姓名
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.DATE}, null, null,
                    CallLog.Calls.DEFAULT_SORT_ORDER);
            if (null == cursor || cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                ContactInfo bean = new ContactInfo();
                bean.contactName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                bean.lastContactTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                bean.phone = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                bean.contactName = bean.phone;
                if (!list.contains(bean)) {
                    list.add(bean);
                }
            }
        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            cursor.close();
        }
        fillContactName(context, list);
        return list;
    }

    public  static void fillContactName(Context context, List<ContactInfo> list) {
        if (list == null) {
            return;
        }
        for (ContactInfo bean : list) {
            //根据电话号码获取名字
            Cursor phoneCursor = null;
            try {
                phoneCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? or "
                                + ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
                        new String[]{bean.phone,bean.phone}, null);
                if (phoneCursor != null) {
                    phoneCursor.moveToPosition(-1);
                    if (phoneCursor.moveToNext()) {
                        String contactName = phoneCursor.getString(phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        bean.contactName = contactName;
                    }
                }
            } catch (Exception e) {
                    e.printStackTrace();
            } finally {
                phoneCursor.close();
            }
        }
    }


    //获取首字母
    private static String getFirstLetter(String key) {
        String res = "#";
        if (!TextUtils.isEmpty(key)) {
            String sortString = key.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                res = sortString.toUpperCase();
            } else {
                if (Pinyin.isChinese(sortString.charAt(0))) {
                    String pin = Pinyin.toPinyin(sortString.charAt(0));
                    if (pin.length() > 0) {
                        return pin.substring(0, 1);
                    }
                    if (pin.matches("[A-Z]")) {
                        res = pin;
                    }
                }
            }
        }
        return res;
    }

}

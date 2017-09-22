package com.example.zhulinping.contactdemo.backandrestore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardComposer;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardConfig;
import com.example.zhulinping.contactdemo.backandrestore.vcard.restore.ImportRequest;
import com.example.zhulinping.contactdemo.backandrestore.vcard.restore.Importutils;
import com.example.zhulinping.contactdemo.backandrestore.vcard.restore.RestoreUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by zhulinping on 2017/9/22.
 */

public class SystemContactsUtils {
    public static final String FORDER_PATH = "/backupandrestore/";
    public static final int TYPE_IMPORT = 1;
    public static final int TYPE_EXPORT = 2;
    public final static int VCARD_VERSION_AUTO_DETECT = 0;
    public final static int VCARD_VERSION_V21 = 1;
    public final static int VCARD_VERSION_V30 = 2;
    public boolean mImportResult;
    private static SystemContactsUtils instance;

    public static SystemContactsUtils getInstance() {
        if (instance == null) {
            instance = new SystemContactsUtils();
        }
        return instance;
    }

    public static String getForderPath() {
        String forderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + FORDER_PATH;
        File file = new File(forderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return forderPath;
    }

    //备份
    public String backup(Context context) {
        String filePath = getForderPath() + "vcard.vcf";
        int exportType = VCardConfig.getVCardTypeFromString("default");
        ContentResolver resolver = context.getContentResolver();
        VCardComposer composer = new VCardComposer(context, exportType, true);
        Writer writer = null;
        try {
            OutputStream outputStream = resolver.openOutputStream(getUri());
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            final Uri contentUriForRawContactsEntity = ContactsContract.RawContactsEntity.CONTENT_URI;
            if (!composer.init(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID},
                    null, null,
                    null, contentUriForRawContactsEntity)) {
                String errorReason = composer.getErrorReason();
                Log.d("zlp", "vcard compose fail reason :" + errorReason);
                return null;
            }
            int total = composer.getCount();
            if (total == 0) {
                return null;
            }
            int current = 1;  // 1-origin
            while (!composer.isAfterLast()) {
                writer.write(composer.createOneEntry());
                // vCard export is quite fast (compared to import), and frequent notifications
                // bother notification bar too much.
                if (current % 100 == 1) {
                    Log.d("zlp", "vcard export progress :" + current);
                }
                current++;
            }
            //TODO:刷新 SD卡
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (composer != null) {
                composer.terminate();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e("zlp", "IOException is thrown during close(). Ignored. " + e);
                }
            }
        }
        return filePath;
    }

    //还原
    public boolean restore(final Context context) {

        Task.callInBackground(new Callable<ArrayList<ImportRequest>>() {
            @Override
            public ArrayList<ImportRequest> call() throws Exception {
                return Importutils.getInstance(context, new Uri[]{getUri()}).getImportRequest();
            }
        }).onSuccess(new Continuation<ArrayList<ImportRequest>, Object>() {
            @Override
            public Object then(Task<ArrayList<ImportRequest>> task) throws Exception {
                final ArrayList<ImportRequest> list = task.getResult();
                if (null == list || list.size() == 0) {
                    mImportResult = false;
                    return null;
                }
                Task.callInBackground(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return RestoreUtils.getInstance(context, list.get(0)).restoreContacts();
                    }
                }).onSuccess(new Continuation<Boolean, Object>() {
                    @Override
                    public Object then(Task<Boolean> task) throws Exception {
                        mImportResult = task.getResult();
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
        return mImportResult;
    }

    public Uri getUri() {
        Uri uri = null;
        String filePath = getForderPath() + "vcard.vcf";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            uri = Uri.fromFile(file);
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

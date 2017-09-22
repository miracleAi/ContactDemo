package com.example.zhulinping.contactdemo.backandrestore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardComposer;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by zhulinping on 2017/9/22.
 */

public class SystemContactsUtils {
    public static final String FORDER_PATH = "/backupandrestore/";

    public static String getForderPath() {
        String forderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + FORDER_PATH;
        File file = new File(forderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return forderPath;
    }

    public static String backup(Context context) {
        String filePath = getForderPath() + "vcard.vcf";
        int exportType = VCardConfig.getVCardTypeFromString("default");
        ContentResolver resolver = context.getContentResolver();
        VCardComposer composer = new VCardComposer(context, exportType, true);
        Writer writer = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream outputStream = resolver.openOutputStream(Uri.fromFile(file));
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
}

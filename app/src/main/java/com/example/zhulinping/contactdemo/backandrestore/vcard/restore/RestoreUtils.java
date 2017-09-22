package com.example.zhulinping.contactdemo.backandrestore.vcard.restore;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.zhulinping.contactdemo.backandrestore.SystemContactsUtils;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardEntry;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardEntryCommitter;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardEntryConstructor;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardEntryHandler;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardInterpreter;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser_V21;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser_V30;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardException;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardNestedException;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardNotSupportedException;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardVersionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zhulinping on 2017/9/22.
 */

public class RestoreUtils {
    private static final String LOG_TAG = "VCardImport";
    private static RestoreUtils instance;
    private  ContentResolver mResolver;
    private  ImportRequest mImportRequest;
    private Context mContext;
    private VCardParser mVCardParser;

    private int mCurrentCount = 0;
    private int mTotalCount = 0;

    private VCardEntryHandler mHandler = new VCardEntryHandler() {
        @Override
        public void onStart() {

        }

        @Override
        public void onEntryCreated(VCardEntry entry) {
            mCurrentCount ++;
            Log.d("zlp","import pregress "+mCurrentCount +"; and total "+mTotalCount);
        }

        @Override
        public void onEnd() {

        }
    };

    public static RestoreUtils getInstance(Context context, ImportRequest request) {
        if (instance == null) {
            instance = new RestoreUtils(context, request);
        }
        return instance;
    }

    public RestoreUtils(Context context, ImportRequest request) {
        mContext = context;
        mResolver = mContext.getContentResolver();
        mImportRequest = request;
    }
    public boolean restoreContacts(){
        final ImportRequest request = mImportRequest;
        final int[] possibleVCardVersions;
        if (request.vcardVersion == SystemContactsUtils.VCARD_VERSION_AUTO_DETECT) {
            /**
             * Note: this code assumes that a given Uri is able to be opened more than once,
             * which may not be true in certain conditions.
             */
            possibleVCardVersions = new int[] {
                    SystemContactsUtils.VCARD_VERSION_V21,
                    SystemContactsUtils.VCARD_VERSION_V30
            };
        } else {
            possibleVCardVersions = new int[] {
                    request.vcardVersion
            };
        }

        final Uri uri = request.uri;
        final int estimatedVCardType = request.estimatedVCardType;
        final String estimatedCharset = request.estimatedCharset;
        final int entryCount = request.entryCount;
        mTotalCount += entryCount;

        final VCardEntryConstructor constructor =
                new VCardEntryConstructor(estimatedVCardType, null,estimatedCharset);
        final VCardEntryCommitter committer = new VCardEntryCommitter(mResolver);
        constructor.addEntryHandler(committer);
        constructor.addEntryHandler(mHandler);

        InputStream is = null;
        boolean successful = false;
        try {
            if (uri != null) {
                Log.i(LOG_TAG, "start importing one vCard (Uri: " + uri + ")");
                is = mResolver.openInputStream(uri);
            } else if (request.data != null){
                Log.i(LOG_TAG, "start importing one vCard (byte[])");
                is = new ByteArrayInputStream(request.data);
            }

            if (is != null) {
                successful = readOneVCard(is, estimatedVCardType, estimatedCharset, constructor,
                        possibleVCardVersions);
            }
        } catch (IOException e) {
            successful = false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return successful;
    }

    private boolean readOneVCard(InputStream is, int vcardType, String charset,
                                 final VCardInterpreter interpreter,
                                 final int[] possibleVCardVersions) {
        boolean successful = false;
        final int length = possibleVCardVersions.length;
        for (int i = 0; i < length; i++) {
            final int vcardVersion = possibleVCardVersions[i];
            try {
                if (i > 0 && (interpreter instanceof VCardEntryConstructor)) {
                    // Let the object clean up internal temporary objects,
                    ((VCardEntryConstructor) interpreter).clear();
                }

                // We need synchronized block here,
                // since we need to handle mCanceled and mVCardParser at once.
                // In the worst case, a user may call cancel() just before creating
                // mVCardParser.
                synchronized (this) {
                    mVCardParser = (vcardVersion == SystemContactsUtils.VCARD_VERSION_V30 ?
                            new VCardParser_V30(vcardType) :
                            new VCardParser_V21(vcardType));
                }
                mVCardParser.parse(is, interpreter);

                successful = true;
                break;
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException was emitted: " + e.getMessage());
            } catch (VCardNestedException e) {
                // This exception should not be thrown here. We should instead handle it
                // in the preprocessing session in ImportVCardActivity, as we don't try
                // to detect the type of given vCard here.
                //
                // TODO: Handle this case appropriately, which should mean we have to have
                // code trying to auto-detect the type of given vCard twice (both in
                // ImportVCardActivity and ImportVCardService).
                Log.e(LOG_TAG, "Nested Exception is found.");
            } catch (VCardNotSupportedException e) {
                Log.e(LOG_TAG, e.toString());
            } catch (VCardVersionException e) {
                if (i == length - 1) {
                    Log.e(LOG_TAG, "Appropriate version for this vCard is not found.");
                } else {
                    // We'll try the other (v30) version.
                }
            } catch (VCardException e) {
                Log.e(LOG_TAG, e.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return successful;
    }
}

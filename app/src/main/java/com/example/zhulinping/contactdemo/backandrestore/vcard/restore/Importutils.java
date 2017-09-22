package com.example.zhulinping.contactdemo.backandrestore.vcard.restore;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.example.zhulinping.contactdemo.backandrestore.SystemContactsUtils;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser_V21;
import com.example.zhulinping.contactdemo.backandrestore.vcard.VCardParser_V30;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardException;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardNestedException;
import com.example.zhulinping.contactdemo.backandrestore.vcardexception.VCardVersionException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

/**
 * Created by zhulinping on 2017/9/22.
 */

public class Importutils {
    private static final String LOG_TAG = "VCardImport";
    private static final String CACHE_FILE_PREFIX = "vcard_import_";


    private boolean mCanceled;
    private PowerManager.WakeLock mWakeLock;
    private VCardParser mVCardParser;
    private final Uri[] mSourceUris;  // Given from a caller.
    private final byte[] mSource;
    private final String mDisplayName;
    private Context context;
    public static Importutils instance;

    public static Importutils getInstance(Context context, final Uri[] sourceUris){
        if(instance == null){
            instance = new Importutils(context,sourceUris);
        }
        return instance;
    }

    public Importutils(Context context,Uri[] sourceUris) {
        mSourceUris = sourceUris;
        mSource = null;
        this.context = context;
        final PowerManager powerManager =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE, LOG_TAG);
        mDisplayName = null;
    }
    public ArrayList<ImportRequest> getImportRequest(){
        mWakeLock.acquire();
        try {
            if (mCanceled == true) {
                Log.i(LOG_TAG, "vCard cache operation is canceled.");
                return null;
            }

            // Uris given from caller applications may not be opened twice: consider when
            // it is not from local storage (e.g. "file:///...") but from some special
            // provider (e.g. "content://...").
            // Thus we have to once copy the content of Uri into local storage, and read
            // it after it.
            //
            // We may be able to read content of each vCard file during copying them
            // to local storage, but currently vCard code does not allow us to do so.
            int cache_index = 0;
            ArrayList<ImportRequest> requests = new ArrayList<ImportRequest>();
            if (mSource != null) {
                try {
                    requests.add(constructImportRequest(context, mSource, null, mDisplayName));
                } catch (VCardException e) {
                    Log.e(LOG_TAG, "Maybe the file is in wrong format", e);
                    return null;
                }
            } else {
                final ContentResolver resolver =
                        context.getContentResolver();
                for (Uri sourceUri : mSourceUris) {
                    String filename = null;
                    // Note: caches are removed by VCardService.
                    while (true) {
                        filename = CACHE_FILE_PREFIX + cache_index + ".vcf";
                        final File file = context.getFileStreamPath(filename);
                        if (!file.exists()) {
                            break;
                        } else {
                            if (cache_index == Integer.MAX_VALUE) {
                                throw new RuntimeException("Exceeded cache limit");
                            }
                            cache_index++;
                        }
                    }
                    Uri localDataUri = null;

                    try {
                        localDataUri = copyTo(context, sourceUri, filename);
                    } catch (SecurityException e) {
                        Log.e(LOG_TAG, "SecurityException", e);
                        return null;
                    }
                    if (mCanceled) {
                        Log.i(LOG_TAG, "vCard cache operation is canceled.");
                        break;
                    }
                    if (localDataUri == null) {
                        Log.w(LOG_TAG, "destUri is null");
                        break;
                    }

                    String displayName = null;
                    Cursor cursor = null;
                    // Try to get a display name from the given Uri. If it fails, we just
                    // pick up the last part of the Uri.
                    try {
                        cursor = resolver.query(sourceUri,
                                new String[]{OpenableColumns.DISPLAY_NAME},
                                null, null, null);
                        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                            if (cursor.getCount() > 1) {
                                Log.w(LOG_TAG, "Unexpected multiple rows: "
                                        + cursor.getCount());
                            }
                            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (index >= 0) {
                                displayName = cursor.getString(index);
                            }
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    if (TextUtils.isEmpty(displayName)) {
                        displayName = sourceUri.getLastPathSegment();
                    }

                    final ImportRequest request;
                    try {
                        request = constructImportRequest(context, null, localDataUri, displayName);
                    } catch (VCardException e) {
                        Log.e(LOG_TAG, "Maybe the file is in wrong format", e);
                        return null;
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Unexpected IOException", e);
                        return null;
                    }
                    if (mCanceled) {
                        Log.i(LOG_TAG, "vCard cache operation is canceled.");
                        return null;
                    }
                    requests.add(request);
                }
            }
            if (!requests.isEmpty()) {
                return requests;
            } else {
                Log.w(LOG_TAG, "Empty import requests. Ignore it.");
                return null;
            }
        } catch (OutOfMemoryError e) {
            Log.e(LOG_TAG, "OutOfMemoryError occured during caching vCard");
            System.gc();
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException during caching vCard", e);
            return null;
        } finally {
            Log.i(LOG_TAG, "Finished caching vCard.");
            mWakeLock.release();
        }
    }

    /**
     * Copy the content of sourceUri to the destination.
     */
    private Uri copyTo(final Context context, final Uri sourceUri, String filename) throws IOException {
        Log.i(LOG_TAG, String.format("Copy a Uri to app local storage (%s -> %s)",
                sourceUri, filename));
        final ContentResolver resolver = context.getContentResolver();
        ReadableByteChannel inputChannel = null;
        WritableByteChannel outputChannel = null;
        Uri destUri = null;
        try {
            inputChannel = Channels.newChannel(resolver.openInputStream(sourceUri));
            destUri = Uri.parse(context.getFileStreamPath(filename).toURI().toString());
            outputChannel = context.openFileOutput(filename, Context.MODE_PRIVATE).getChannel();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
            while (inputChannel.read(buffer) != -1) {
                if (mCanceled) {
                    Log.d(LOG_TAG, "Canceled during caching " + sourceUri);
                    return null;
                }
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        } finally {
            if (inputChannel != null) {
                try {
                    inputChannel.close();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to close inputChannel.");
                }
            }
            if (outputChannel != null) {
                try {
                    outputChannel.close();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to close outputChannel");
                }
            }
        }
        return destUri;
    }

    /**
     * Reads localDataUri (possibly multiple times) and constructs {@link ImportRequest} from
     * its content.
     *
     * @arg localDataUri Uri actually used for the import. Should be stored in
     * app local storage, as we cannot guarantee other types of Uris can be read
     * multiple times. This variable populates {@link ImportRequest#uri}.
     * @arg displayName Used for displaying information to the user. This variable populates
     * {@link ImportRequest#displayName}.
     */
    private ImportRequest constructImportRequest(Context context, final byte[] data,
                                                 final Uri localDataUri, final String displayName)
            throws IOException, VCardException {
        final ContentResolver resolver = context.getContentResolver();
        VCardEntryCounter counter = null;
        VCardSourceDetector detector = null;
        int vcardVersion = SystemContactsUtils.VCARD_VERSION_V21;
        try {
            boolean shouldUseV30 = false;
            InputStream is;
            if (data != null) {
                is = new ByteArrayInputStream(data);
            } else {
                is = resolver.openInputStream(localDataUri);
            }
            mVCardParser = new VCardParser_V21();
            try {
                counter = new VCardEntryCounter();
                detector = new VCardSourceDetector();
                mVCardParser.addInterpreter(counter);
                mVCardParser.addInterpreter(detector);
                mVCardParser.parse(is);
            } catch (VCardVersionException e1) {
                try {
                    is.close();
                } catch (IOException e) {
                }

                shouldUseV30 = true;
                if (data != null) {
                    is = new ByteArrayInputStream(data);
                } else {
                    is = resolver.openInputStream(localDataUri);
                }
                mVCardParser = new VCardParser_V30();
                try {
                    counter = new VCardEntryCounter();
                    detector = new VCardSourceDetector();
                    mVCardParser.addInterpreter(counter);
                    mVCardParser.addInterpreter(detector);
                    mVCardParser.parse(is);
                } catch (VCardVersionException e2) {
                    throw new VCardException("vCard with unspported version.");
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }

            vcardVersion = shouldUseV30 ? SystemContactsUtils.VCARD_VERSION_V30 : SystemContactsUtils.VCARD_VERSION_V21;
        } catch (VCardNestedException e) {
            Log.w(LOG_TAG, "Nested Exception is found (it may be false-positive).");
            // Go through without throwing the Exception, as we may be able to detect the
            // version before it
        }
        return new ImportRequest(
                data, localDataUri, displayName,
                detector.getEstimatedType(),
                detector.getEstimatedCharset(),
                vcardVersion, counter.getCount());
    }
}

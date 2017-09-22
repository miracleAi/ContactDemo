package com.example.zhulinping.contactdemo.backandrestore.vcardexception;

/**
 * Created by zhulinping on 2017/9/22.
 */

/**
 * VCardException used only when the version of the vCard is different.
 */
public class VCardVersionException extends VCardException {
    public VCardVersionException() {
        super();
    }
    public VCardVersionException(String message) {
        super(message);
    }
}

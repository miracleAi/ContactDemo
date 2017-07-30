package com.example.zhulinping.contactdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.zhulinping.contactdemo.contactdata.ContactDataHelper;
import com.example.zhulinping.contactdemo.diaplay.ContactFragment;
import com.example.zhulinping.contactdemo.diaplay.ContactPresenter;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String CONTACT_FRAGMENT = "MainActivity.contactFtagment";
    private static final String REQUIRED_PERMISSIONS = Manifest.permission.READ_CONTACTS;
    private static final int PERMISSIONS_REQUEST = 0;
    protected FragmentManager mFragmentManager;
    protected Fragment mCurrentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        if(checkPermission()) {
            selectFragment(CONTACT_FRAGMENT);
        }else{
            requestPermissions();
        }

    }
    private void selectFragment(String tag) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (null != mCurrentFragment) {
            ft.hide(mCurrentFragment);
        }
        if (CONTACT_FRAGMENT.equals(tag)) {
            Fragment fragment = mFragmentManager.findFragmentByTag(CONTACT_FRAGMENT);
            if (null == fragment) {
                fragment = new ContactFragment();
                ft.add(R.id.activity_content, fragment, CONTACT_FRAGMENT);
            } else {
                ft.show(fragment);
            }
            mCurrentFragment = fragment;
        }
        ft.commitAllowingStateLoss();
        new ContactPresenter(new ContactDataHelper(getApplicationContext()),(ContactFragment)mCurrentFragment);
    }
    private boolean checkPermission() {
            if (ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{REQUIRED_PERMISSIONS},
                PERMISSIONS_REQUEST);
    }
    private boolean allResultGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PERMISSIONS_REQUEST == requestCode) {
            if (allResultGranted(grantResults)) {
                selectFragment(CONTACT_FRAGMENT);
            }
        }
    }
}

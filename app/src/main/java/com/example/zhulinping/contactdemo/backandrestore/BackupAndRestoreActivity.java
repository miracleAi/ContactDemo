package com.example.zhulinping.contactdemo.backandrestore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zhulinping.contactdemo.R;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BackupAndRestoreActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_backup)
    Button btnBackup;
    @BindView(R.id.tv_backup_path)
    TextView tvBackupPath;
    @BindView(R.id.btn_restore)
    Button btnRestore;
    @BindView(R.id.tv_rstore_path)
    TextView tvRstorePath;
    @BindView(R.id.tv_result)
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_and_restore);
        ButterKnife.bind(this);
        btnBackup.setOnClickListener(this);
        btnRestore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_backup:
                contactsBackup();
                break;
            case R.id.btn_restore:
                contactsRestore();
                break;
        }
    }

    //通讯录备份
    public void contactsBackup() {
        tvBackupPath.setText(" contacts backuping……");
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return SystemContactsUtils.getInstance().backup(BackupAndRestoreActivity.this);
            }
        }).onSuccess(new Continuation<String, Object>() {
            @Override
            public Object then(Task<String> task) throws Exception {
                String path = task.getResult();
                if (null != path && !path.equals("")) {
                    tvBackupPath.setText(path);
                    tvResult.setText("backup success!");
                } else {
                    tvBackupPath.setText("null");
                    tvResult.setText("backup fail");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }
    //通讯录还原
    public void contactsRestore(){
        tvRstorePath.setText("contacts restoring ……");
        Task.callInBackground(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return SystemContactsUtils.getInstance().restore(BackupAndRestoreActivity.this);
            }
        }).onSuccess(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                tvRstorePath.setText(" restore done");
                boolean result = task.getResult();
                if(result){
                    tvResult.setText("restore success");
                }else {
                    tvResult.setText("restore fail");
                }
                return null;
            }
        },Task.UI_THREAD_EXECUTOR);
    }
}

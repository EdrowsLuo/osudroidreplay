package com.edlplan.onlyloadreplay;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.edlplan.osu.droid.replay.OdrConfig;
import com.edlplan.osu.droid.replay.OdrDatabase;
import com.edlplan.osu.droid.replay.OsuDroidReplayPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        if (getIntent().getData() != null) {
            String path = getIntent().getData().getPath();
            System.out.println("path: " + path);
            File file = new File(path);
            if ((!file.exists()) || file.isDirectory()) {
                Toast.makeText(MainActivity.this, "错误的文件", Toast.LENGTH_SHORT).show();
                super.onStart();
                finish();
                return;
            }
            try {
                OsuDroidReplayPack.ReplayEntry entry = OsuDroidReplayPack.unpack(new FileInputStream(file));
                File rep = new File(OdrConfig.getScoreDir(), entry.replay.getReplayFileName());
                if (!rep.exists()) {
                    if (!rep.createNewFile()) {
                        Toast.makeText(MainActivity.this, "无法创建文件", Toast.LENGTH_SHORT).show();
                        super.onStart();
                        finish();
                        return;
                    }
                }
                FileOutputStream outputStream = new FileOutputStream(rep);
                outputStream.write(entry.replayFile);
                outputStream.close();
                entry.replay.setReplayFile(rep.getAbsolutePath());
                if (OdrDatabase.get().write(entry.replay) != -1) {
                    Toast.makeText(this, "成功导入rep", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "数据库写入失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "错误: " + e.toString(), Toast.LENGTH_SHORT).show();
                super.onStart();
                finish();
                return;
            }
        }
        super.onStart();
    }
}

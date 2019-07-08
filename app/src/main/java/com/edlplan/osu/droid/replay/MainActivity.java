package com.edlplan.osu.droid.replay;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static MainActivity activity;

    private OdrAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }



        activity = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!OdrDatabase.get().available()) {
                    Snackbar.make(fab, "数据库没有找到，无法导入replay", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle("replay文件路径(.edr)");
                dialog.setContentView(R.layout.dialog_add);
                dialog.findViewById(R.id.import_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = ((EditText) dialog.findViewById(R.id.editText)).getText().toString();
                        File file = new File(path);
                        if ((!file.exists()) || file.isDirectory()) {
                            Toast.makeText(MainActivity.this, "错误的文件", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        try {
                            OsuDroidReplayPack.ReplayEntry entry = OsuDroidReplayPack.unpack(new FileInputStream(file));
                            File rep = new File(OdrConfig.getScoreDir(), entry.replay.getReplayFileName());
                            if (!rep.exists()) {
                                if (!rep.createNewFile()) {
                                    Toast.makeText(MainActivity.this, "无法创建文件", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    return;
                                }
                            }
                            FileOutputStream outputStream = new FileOutputStream(rep);
                            outputStream.write(entry.replayFile);
                            outputStream.close();
                            entry.replay.setReplayFile(rep.getAbsolutePath());
                            if (OdrDatabase.get().write(entry.replay) != -1) {
                                Snackbar.make(fab, "成功导入rep", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "数据库写入失败", Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "错误: " + e.toString(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                    }
                });
                dialog.show();
            }
        });


        final FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!OdrDatabase.get().available()) {
                    Snackbar.make(fab2, "数据库没有找到，功能无法正常使用", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle("replay文件路径(.edr)");
                dialog.setContentView(R.layout.dialog_search);
                dialog.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String keytext = ((EditText) dialog.findViewById(R.id.editText)).getText().toString().toLowerCase();

                        List<OsuDroidReplay> reps = new ArrayList<>();
                        for (OsuDroidReplay replay : OdrDatabase.get().getReplays()) {
                            if (replay.getFileName().toLowerCase().contains(keytext)) {
                                reps.add(replay);
                            }
                        }

                        adapter.setReplays(reps);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        RecyclerView recyclerView = findViewById(R.id.main_scroll);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        adapter = new OdrAdapter(OdrDatabase.get().getReplays());
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (!OdrDatabase.get().available()) {
            Snackbar.make(fab, "没有找到replay数据库位置", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(fab, "成功找到replay数据库", Snackbar.LENGTH_SHORT).show();
        }
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

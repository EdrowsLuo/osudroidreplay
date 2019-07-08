package com.edlplan.osu.droid.replay;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OdrAdapter extends RecyclerView.Adapter<OdrAdapter.OdrViewHolder> {

    private List<OsuDroidReplay> replays;

    public OdrAdapter(@NonNull List<OsuDroidReplay> replays) {
        this.replays = new ArrayList<>();
        for (int i = 0; i < replays.size(); i++) {
            this.replays.add(replays.get(replays.size() - 1 - i));
        }
    }

    public void setReplays(List<OsuDroidReplay> replays) {
        this.replays = new ArrayList<>();
        for (int i = 0; i < replays.size(); i++) {
            this.replays.add(replays.get(replays.size() - 1 - i));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OdrViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new OdrViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.replay_entry, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final OdrViewHolder odrViewHolder, int i) {

        StringBuilder stringBuilder = new StringBuilder();
        final OsuDroidReplay replay = replays.get(i);
        stringBuilder.append("dif: ");
        stringBuilder.append(replay.getFileName().subSequence(replay.getFileName().indexOf('/') + 1, replay.getFileName().length()));
        stringBuilder.append('\n');
        stringBuilder.append("rank: ").append(replay.getMark());
        stringBuilder.append('\n');
        stringBuilder.append(String.format("combo: %-5d acc: %-5.2f%%  date: %tD",
                replay.getCombo(),
                replay.getAccuracy()*100,
                new Date(replay.getTime())));

        odrViewHolder.title.setText(stringBuilder.toString());
        odrViewHolder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "确认导出replay?", Snackbar.LENGTH_LONG)
                        .setAction("导出", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    final File file = new File(
                                            OdrConfig.getMainDatabase().getParentFile(),
                                            String.format("%s [%s]-%d.edr",
                                                    replay.getFileName().subSequence(replay.getFileName().indexOf('/') + 1, replay.getFileName().lastIndexOf('.')),
                                                    replay.getPlayerName(),
                                                    replay.getTime())
                                    );
                                    OsuDroidReplayPack.packTo(file, replay);

                                    Snackbar.make(v, String.format("导出成功，路径为：%s", file.getAbsolutePath()), Snackbar.LENGTH_LONG).setAction("分享", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setDataAndType(FileProvider.getUriForFile(
                                                    MainActivity.activity,
                                                    "com.edlplan.osu.droid.replay",
                                                    file),"*/*");
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            MainActivity.activity.startActivityForResult(intent, 0);
                                        }
                                    }).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(v.getContext(), "导出失败", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return replays.size();
    }

    public static class OdrViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout main;

        public TextView title;

        public OdrViewHolder(@NonNull View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.entry_main);
            title = itemView.findViewById(R.id.entry_title);
        }
    }

}


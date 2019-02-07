package musicplayer.com.musicplayer;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongFragment extends Fragment {

    private Button mBtnPlay, mBtnForward, mBtnPrevious, mBtnRewind, mBtnNext;
    //private MediaPlayer mediaPlayer;
    private SeekBar mSeekBar;
    private TextView mStartTime, mEndTime, mSongName;
    private Thread thread;
    private String songName;
    private String songPath;
    private String position;
    private List<SongList> songLists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        uiUpdate(view);

        Bundle bundle = getArguments();
        if (bundle.getString("songList") != null) {

            Gson gson = new Gson();
            TypeToken<List<SongList>> token = new TypeToken<List<SongList>>() {
            };
            songLists = gson.fromJson(bundle.getString("songList"), token.getType());

            position = bundle.getString("position");

            songName = songLists.get(Integer.valueOf(position)).getTitle();
            songPath = songLists.get(Integer.valueOf(position)).getPath();
            mSongName.setText(songName);
            Log.i("ccccccc", songName);

            getActivity().stopService(new Intent(getActivity(), SongService.class));
            getActivity().startService(new Intent(getActivity(), SongService.class)
                    .putExtra("sonUri", songPath));
            mBtnPlay.setText(R.string.pause);

            mBtnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mBtnPlay.getText().equals("Pause")) {
                        mBtnPlay.setText(R.string.play);
                        getActivity().startService(new Intent(getActivity(), SongService.class)
                                .putExtra("pauseSong", "pauseSong"));
                    } else {
                        mBtnPlay.setText(R.string.pause);
                        getActivity().startService(new Intent(getActivity(), SongService.class)
                                .putExtra("playSong", "playSong"));
                    }
                }
            });

            mBtnForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().startService(new Intent(getActivity(), SongService.class)
                            .putExtra("forwardSong", "forwardSong"));
                }
            });

            mBtnRewind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().startService(new Intent(getActivity(), SongService.class)
                            .putExtra("rewindSong", "rewindSong"));
                }
            });

            mBtnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String nextSongName = "", nextSongPath = "";
                    for (int i = 0; i <= songLists.size() - 1; i++) {
                        if (songName.equals(songLists.get(i).getTitle())) {
                            songName = "";
                            nextSongName = songLists.get(i + 1).getTitle();
                            nextSongPath = songLists.get(i + 1).getPath();
                        }
                    }
                    songName = nextSongName;
                    mSongName.setText(nextSongName);
                    getActivity().startService(new Intent(getActivity(), SongService.class)
                            .putExtra("nextSong", "nextSong")
                            .putExtra("sonUri", nextSongPath));
                }
            });

            mBtnPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String previousSongName = "", nextSongPath = "";
                    for (int i = 0; i <= songLists.size() - 1; i++) {
                        if (songName.equals(songLists.get(i).getTitle())) {
                            songName = "";
                            if (i == 0) {
                                i = 1;
                                previousSongName = songLists.get(i - 1).getTitle();
                                nextSongPath = songLists.get(i - 1).getPath();
                            } else {
                                previousSongName = songLists.get(i - 1).getTitle();
                                nextSongPath = songLists.get(i - 1).getPath();
                            }
                        }
                    }
                    songName = previousSongName;
                    mSongName.setText(previousSongName);
                    getActivity().startService(new Intent(getActivity(), SongService.class)
                            .putExtra("previousSong", "previousSong")
                            .putExtra("sonUri", nextSongPath));
                }
            });

        }

        return view;
    }

    public void uiUpdate(View view) {
        mBtnPlay = view.findViewById(R.id.btn_play);
        mBtnForward = view.findViewById(R.id.btn_forward);
        mBtnNext = view.findViewById(R.id.btn_next);
        mBtnPrevious = view.findViewById(R.id.btn_previous);
        mBtnRewind = view.findViewById(R.id.btn_rewind);
        mSeekBar = view.findViewById(R.id.seekBar);
        mStartTime = view.findViewById(R.id.tv_start_time);
        mEndTime = view.findViewById(R.id.tv_end_time);
        mSongName = view.findViewById(R.id.tv_song_name);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.service.activity");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }


    //Using BroadCast Receiver
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String startTimeInSec = intent.getStringExtra("startTimeInSec");
            String endTimeInSec = intent.getStringExtra("endTimeInSec");
            int position = intent.getIntExtra("position", 0);
            int songDuration = intent.getIntExtra("songDuration", 0);
            mStartTime.setText(startTimeInSec);
            mEndTime.setText(endTimeInSec);
            mSeekBar.setProgress(position);
            mSeekBar.setMax(songDuration);
            //seekBarThread(position, songDuration);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    /*@SuppressLint("DefaultLocale")
    public void seekBarThread(final int position, final int songDuration) {
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           *//* int position = mediaPlayer.getCurrentPosition();
                            int songDuration = mediaPlayer.getDuration();*//*
                            mSeekBar.setMax(songDuration);
                            mSeekBar.setProgress(position);
                            String startTimeInSec = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(position),
                                    TimeUnit.MILLISECONDS.toSeconds(position) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                            String endTimeInSec = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(songDuration - position),
                                    TimeUnit.MILLISECONDS.toSeconds(songDuration - position) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration - position)));
                            mStartTime.setText(startTimeInSec);
                            mEndTime.setText(endTimeInSec);
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }*/

}

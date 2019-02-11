package musicplayer.com.musicplayer;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongFragment extends Fragment {

    private Button mBtnPlay, mBtnForward, mBtnPrevious, mBtnRewind, mBtnNext, mBtnVolumeUp, mBtnVolumeDown;
    //private MediaPlayer mediaPlayer;
    private SeekBar mSeekBarVolume = null;
    private TextView mStartTime, mEndTime, mSongName;
    private ImageView mSongImage;
    private Thread thread;
    private String songName;
    private String songPath;
    private String position;
    private List<SongList> songLists;
    private AudioManager mAudioManager = null;
    private int volumeMax, currentVolume;
    private String musicIsPlaying;

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
            //mSongImage.set
            Log.i("ccccccc", songName);

            getActivity().stopService(new Intent(getActivity(), SongService.class));
            getActivity().startService(new Intent(getActivity(), SongService.class)
                    .putExtra("sonUri", songPath));

            //Volume
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            volumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mSeekBarVolume.setMax(volumeMax);
            mSeekBarVolume.setProgress(currentVolume);

            mSeekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            });

            mBtnVolumeUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                    currentVolume = currentVolume + 1;
                    mSeekBarVolume.setProgress(currentVolume);
                }
            });

            mBtnVolumeDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                    currentVolume = currentVolume - 1;
                    mSeekBarVolume.setProgress(currentVolume);
                }
            });


            //mBtnPlay.setText(R.string.pause);
            musicIsPlaying = "Pause";

            mBtnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (musicIsPlaying.equals("Pause")) {
                        //mBtnPlay.setText(R.string.play);
                        musicIsPlaying = "Play";
                        mBtnPlay.setBackgroundResource(R.drawable.play);
                        getActivity().startService(new Intent(getActivity(), SongService.class)
                                .putExtra("pauseSong", "pauseSong"));
                    } else {
                        //mBtnPlay.setText(R.string.pause);
                        musicIsPlaying = "Pause";
                        mBtnPlay.setBackgroundResource(R.drawable.pause);
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
        mSeekBarVolume = view.findViewById(R.id.sb_volume);
        mStartTime = view.findViewById(R.id.tv_songDuration);
        mSongName = view.findViewById(R.id.tv_song_name);
        mBtnVolumeUp = view.findViewById(R.id.btn_volumeUp);
        mBtnVolumeDown = view.findViewById(R.id.btn_volumeDown);
        mSongImage = view.findViewById(R.id.imageView);


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
            //String endTimeInSec = intent.getStringExtra("endTimeInSec");
            //int position = intent.getIntExtra("position", 0);
            int songDuration = intent.getIntExtra("songDuration", 0);
            mStartTime.setText(startTimeInSec);
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

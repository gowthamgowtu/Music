package musicplayer.com.musicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button mBtnPlay, mBtnForward, mBtnPrevious, mBtnRewind, mBtnNext;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView startTime, endTime, mSongName;
    private Thread thread;
    private String songName;
    private String songPath;
    private int position;
    private List<SongList> songLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiUpdate();

        if (getIntent().getStringExtra("songList") != null) {

            Gson gson = new Gson();
            TypeToken<List<SongList>> token = new TypeToken<List<SongList>>() {
            };
            songLists = gson.fromJson(getIntent().getStringExtra("songList"), token.getType());

            position = getIntent().getIntExtra("position", 0);

            songName = songLists.get(position).getTitle();
            songPath = songLists.get(position).getPath();
            mSongName.setText(songName);
            Log.i("ccccccc", songName);

            Uri songUri = Uri.parse(songPath);
            mediaPlayer = new MediaPlayer();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);
            seekBar.setMax(mediaPlayer.getDuration());
            if (mediaPlayer.isPlaying()) {
                pauseMusic();
            } else {
                startMusic();
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int duration, boolean b) {

                    if (b) {
                        mediaPlayer.seekTo(duration);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mBtnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaPlayer.isPlaying()) {
                        pauseMusic();
                    } else {
                        startMusic();
                    }
                }
            });

            mBtnForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                }
            });

            mBtnRewind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
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
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    startService(new Intent(MainActivity.this, SongService.class)
                            .putExtra("sonUri", nextSongPath));
                    Uri uri = Uri.parse(nextSongPath);
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    mBtnPlay.setText(R.string.pause);
                }
            });

            mBtnPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String nextSongName = "", nextSongPath = "";
                    for (int i = 0; i <= songLists.size() - 1; i++) {
                        if (songName.equals(songLists.get(i).getTitle())) {
                            songName = "";
                            if (i == 0) {
                                i = 1;
                                nextSongName = songLists.get(i - 1).getTitle();
                                nextSongPath = songLists.get(i - 1).getPath();
                            } else {
                                nextSongName = songLists.get(i - 1).getTitle();
                                nextSongPath = songLists.get(i - 1).getPath();
                            }
                        }
                    }
                    songName = nextSongName;
                    mSongName.setText(nextSongName);
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    startService(new Intent(MainActivity.this, SongService.class)
                            .putExtra("sonUri", nextSongPath));
                    Uri uri = Uri.parse(nextSongPath);
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    mBtnPlay.setText(R.string.pause);
                }
            });

        }

    }

    public void uiUpdate() {
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnForward = (Button) findViewById(R.id.btn_forward);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnPrevious = (Button) findViewById(R.id.btn_previous);
        mBtnRewind = (Button) findViewById(R.id.btn_rewind);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        startTime = (TextView) findViewById(R.id.tv_start_time);
        endTime = (TextView) findViewById(R.id.tv_end_time);
        mSongName = (TextView) findViewById(R.id.tv_song_name);
    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mBtnPlay.setText(R.string.play);
        }
    }

    public void startMusic() {
        if (mediaPlayer != null) {
            startService(new Intent(this, SongService.class)
                    .putExtra("sonUri", songPath));
            mediaPlayer.start();
            seekBarThread();
            mBtnPlay.setText(R.string.pause);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            startService(new Intent(this, SongService.class)
                    .putExtra("sonUri", songPath));
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @SuppressLint("DefaultLocale")
    public void seekBarThread() {
        thread = new Thread() {

            @Override
            public void run() {

                try {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null) {
                                    int position = mediaPlayer.getCurrentPosition();
                                    int songDuration = mediaPlayer.getDuration();
                                    seekBar.setMax(songDuration);
                                    seekBar.setProgress(position);
                                    String startTimeInSec = String.format("%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(position),
                                            TimeUnit.MILLISECONDS.toSeconds(position) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
                                    String endTimeInSec = String.format("%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(songDuration - position),
                                            TimeUnit.MILLISECONDS.toSeconds(songDuration - position) -
                                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration - position)));
                                    Log.i("startTime->", startTimeInSec);
                                    startTime.setText(startTimeInSec);
                                    endTime.setText(endTimeInSec);
                                }
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}

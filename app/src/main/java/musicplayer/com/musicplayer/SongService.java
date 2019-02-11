package musicplayer.com.musicplayer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class SongService extends Service {

    private MediaPlayer mediaPlayer;
    private String songPath;
    private int position, duration;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        songPath = intent.getStringExtra("sonUri");
        if (intent.getStringExtra("pauseSong") != null && intent.getStringExtra("pauseSong").equals("pauseSong")) {
            mediaPlayer.pause();
        } else if (intent.getStringExtra("playSong") != null && intent.getStringExtra("playSong").equals("playSong")) {
            mediaPlayer.start();
        } else if (intent.getStringExtra("forwardSong") != null && intent.getStringExtra("forwardSong").equals("forwardSong")) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
        } else if (intent.getStringExtra("rewindSong") != null && intent.getStringExtra("rewindSong").equals("rewindSong")) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
        } else if (intent.getStringExtra("previousSong") != null && intent.getStringExtra("previousSong").equals("previousSong")) {
            stopMusic();
            startMusic();
        } else if (intent.getStringExtra("nextSong") != null && intent.getStringExtra("nextSong").equals("nextSong")) {
            stopMusic();
            startMusic();
        } else {
            startMusic();
        }

        new SongDurationAsyncTask().execute();
        Log.i("Service Started", "Service Started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMusic();
        Log.i("Service stopped", "Service stopped");
    }

    public void startMusic() {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(songPath));
        mediaPlayer.start();
    }

    public void stopMusic() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @SuppressLint("DefaultLocale")
    class SongDurationAsyncTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String startTimeInSec = values[0];
            String endTimeInSec = values[1];
            Intent intent = new Intent("action.service.activity");
            intent.putExtra("startTimeInSec",startTimeInSec);
            intent.putExtra("endTimeInSec",endTimeInSec);
            intent.putExtra("position", mediaPlayer.getCurrentPosition());
            intent.putExtra("songDuration", mediaPlayer.getDuration());
            sendBroadcast(intent);

            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            duration = mediaPlayer.getDuration();
            position = mediaPlayer.getCurrentPosition();
            String startTimeInSec = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(position),
                    TimeUnit.MILLISECONDS.toSeconds(position) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)));
            String endTimeInSec = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration - position),
                    TimeUnit.MILLISECONDS.toSeconds(duration - position) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration - position)));
            Log.i("StartTime", startTimeInSec);
            publishProgress(startTimeInSec, endTimeInSec);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}

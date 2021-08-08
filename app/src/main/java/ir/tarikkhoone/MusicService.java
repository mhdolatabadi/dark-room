package ir.tarikkhoone;

import static android.media.PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;

import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.widget.MediaController;

import androidx.annotation.RequiresApi;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;
    private final IBinder musicBind = new MusicBinder();


    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public MusicService() {

    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            Log.d("SRV", String.valueOf(MusicService.this));
            Log.d("SRV", String.valueOf(musicBind));
            return MusicService.this;
        }
    }

    public void setShuffle() {
        if (shuffle) shuffle = false;
        else shuffle = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();

    }


    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback

        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void playSong() {
        player = new MediaPlayer();
        initMusicPlayer();



        Log.d("player", String.valueOf(player.getAudioSessionId()));

        //get song
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();

    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pause() {
        player.pause();
    }

    public void seek(int position) {
        player.seekTo(position);
    }

    public void start() {
        player.start();
    }

    public void playPrevious() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        release();
        playSong();
    }

    public void release(){
        player.pause();
        player.stop();
        player.release();
    }


    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
        }
        release();
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
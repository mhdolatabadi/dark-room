package ir.tarikkhoone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ir.tarikkhoone.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, MediaController.MediaPlayerControl {
    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_STORAGE = 0;
    private MusicService musicSrv;
    private boolean paused = false, playbackPaused = false;
    private MediaController controller;
    private boolean musicBound = false;
    private ArrayList<Song> songList;
    private Intent playIntent;


    private final ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestStoragePermission();
        songList = new ArrayList<Song>();
        getSongList();
        Collections.sort(songList, Comparator.comparing(Song::getTitle));
    }

    public void getSongList() {
        ContentResolver musicResolver = this.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        @SuppressLint("Recycle") Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            this.startService(playIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                BottomNavigationView navView = findViewById(R.id.nav_view);
                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                        .build();
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(binding.navView, navController);
            } else {
                requestStoragePermission();
            }
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_STORAGE);
    }

    public void songPicked(View view) {
        musicSrv.release();
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if (playbackPaused) {
            playbackPaused = false;
        }
        setController();
        controller.show();
    }

    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev() {
        musicSrv.playPrevious();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void setController() {
        if (controller == null)
            controller = new MusicController(this);
        controller.setPrevNextListeners(v -> playNext(), v -> playPrev());
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }


    @Override
    public void onDestroy() {
        this.stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }


    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getPosition();
        else return 0;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pause();
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    public void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.start();
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
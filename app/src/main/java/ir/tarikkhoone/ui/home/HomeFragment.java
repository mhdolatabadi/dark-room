package ir.tarikkhoone.ui.home;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import ir.tarikkhoone.MusicController;
import ir.tarikkhoone.MusicService;
import ir.tarikkhoone.R;
import ir.tarikkhoone.Song;
import ir.tarikkhoone.SongAdapter;
import ir.tarikkhoone.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment{

    private FragmentHomeBinding binding;
    private ArrayList<Song> songList;

    private MusicService musicSrv;
    private Intent playIntent;



    private MediaController controller;

    private boolean paused = false, playbackPaused = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        songList = new ArrayList<Song>();
        getSongList();
        Collections.sort(songList, Comparator.comparing(Song::getTitle));

        SongAdapter songAdapter = new SongAdapter(songList);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.song_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(songAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                musicSrv.stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSongList() {
        ContentResolver musicResolver = requireActivity().getContentResolver();
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



}
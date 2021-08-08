package ir.tarikkhoone;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

public class Song {
    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist) {
        id = songID;
        title = songTitle;
        artist = songArtist;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}

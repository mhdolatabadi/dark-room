package ir.tarikkhoone;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import ir.tarikkhoone.ui.home.HomeFragment;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private final ArrayList<Song> songs;






    public SongAdapter(ArrayList<Song> theSongs) {
        songs = theSongs;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.song, parent, false);
        listItem.setTag(0);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Song current = songs.get(position);
        holder.bind(current.getTitle(), current.getArtist(), position);
holder.item.setTag(position);


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songView;
        TextView artistView;
        View item;
        private ViewHolder(@NotNull View itemView) {
            super(itemView);
            songView = (TextView) itemView.findViewById(R.id.song_title);
            artistView = (TextView) itemView.findViewById(R.id.song_artist);
            item = itemView;
            item.setTag(0);
        }
        public void bind(String title, String artist, int position) {
            songView.setText(title);
            artistView.setText(artist);
            item.setTag(position);
        }

    }
}

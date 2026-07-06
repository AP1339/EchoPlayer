package com.snehant.echoplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.snehant.echoplayer.R;
import com.snehant.echoplayer.interfaces.OnSongClickListener;
import com.snehant.echoplayer.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs = new ArrayList<>();
    private OnSongClickListener listener;

    public SongAdapter(OnSongClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.txtSongTitle.setText(song.getTitle());
        holder.txtArtistName.setText(song.getArtist());

        long durationMs = song.getDuration();
        String formattedDuration = formatDuration(durationMs);
        holder.txtDuration.setText(formattedDuration);

        holder.imgSong.setVisibility(View.GONE);
        holder.txtPlaceholder.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSongs(List<Song> songs) {
        if (songs != null && songs.size() > 5) {
            this.songs = new ArrayList<>(songs.subList(0, 5));
        } else {
            this.songs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void setAllSongs(List<Song> songs) {
        this.songs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatDuration(long durationMs) {
        long minutes = (durationMs / 1000) / 60;
        long seconds = (durationMs / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView txtPlaceholder;
        TextView txtSongTitle;
        TextView txtArtistName;
        TextView txtDuration;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            txtPlaceholder = itemView.findViewById(R.id.txtPlaceholder);
            txtSongTitle = itemView.findViewById(R.id.txtSongTitle);
            txtArtistName = itemView.findViewById(R.id.txtArtistName);
            txtDuration = itemView.findViewById(R.id.txtDuration);
        }
    }
}
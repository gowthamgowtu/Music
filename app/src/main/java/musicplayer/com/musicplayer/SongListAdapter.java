package musicplayer.com.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

    private List<SongList> mSongList;
    private Context mContext;

    public SongListAdapter(Context context, List<SongList> songLists) {
        this.mContext = context;
        this.mSongList = songLists;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvTitle, mTvArtist;
        private LinearLayout mSongListLayout;


        public ViewHolder(@NonNull View view) {
            super(view);

            mTvTitle = (TextView) view.findViewById(R.id.tv_title);
            mTvArtist = (TextView) view.findViewById(R.id.tv_artist);
            mSongListLayout = (LinearLayout) view.findViewById(R.id.songListLayout);
        }
    }


    @NonNull
    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_list, viewGroup, false);
        return new SongListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.mTvTitle.setText("Song Title:-" + mSongList.get(position).getTitle());
        viewHolder.mTvArtist.setText("Artist name:-" + mSongList.get(position).getArtist());

        viewHolder.mSongListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                String songList = gson.toJson(mSongList);
               /* Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("songList",songList);
                intent.putExtra("position",position);
                mContext.startActivity(intent);*/
                SongFragment songFragment = new SongFragment();
                Bundle bundle = new Bundle();
                FragmentManager fragmentManager = ((Activity) mContext).getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.layout, songFragment, "songFrag");
                bundle.putString("songList", songList);
                bundle.putString("position", String.valueOf(position));
                songFragment.setArguments(bundle);
                fragmentTransaction.addToBackStack("songFrag");
                mContext.stopService(new Intent(mContext, SongService.class));
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }
}

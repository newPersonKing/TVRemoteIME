package xllib;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afap.ijkplayer.R;
/**
 * Created by kingt on 2018/2/3.
 */

public class PlayListItemAdapter extends RecyclerView.Adapter  {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_xl_play_list_item, null);
        view.setFocusable(true);
        return new PlayListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlayListItemViewHolder tfHolder = (PlayListItemViewHolder)holder;
        tfHolder.setPlayListItem(DownloadManager.instance().taskInstance().getPlayList().get(position));
    }

    @Override
    public int getItemCount() {
        return DownloadManager.instance().taskInstance().getPlayList().size();
    }

    private View.OnClickListener onClickListener;
    public void setOnPlayListItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    class PlayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView ivPlayingState;
        TextView tvPlayingName;
        TextView tvPlayingSize;
        public PlayListItemViewHolder(View itemView) {
            super(itemView);
            this.itemView.setOnClickListener(this);
            ivPlayingState = (ImageView)itemView.findViewById(R.id.iv_playing_state);
            tvPlayingName = (TextView)itemView.findViewById(R.id.tv_playing_name);
            tvPlayingSize = (TextView)itemView.findViewById(R.id.tv_playing_size);
        }

        @Override
        public void onClick(View view) {
            if(onClickListener != null)onClickListener.onClick(view);
        }

        void setPlayListItem(PlayListItem playListItem){
            ivPlayingState.setVisibility(playListItem == DownloadManager.instance().taskInstance().getCurrentPlayItem() ? View.VISIBLE : View.GONE);
            tvPlayingName.setText(playListItem.getName());
            tvPlayingSize.setText(FileUtils.convertFileSize(playListItem.getSize()));
            this.itemView.setTag(playListItem);
        }
    }
}

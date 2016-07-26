package com.sergey.githubusers;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sergey.githubusers.pojo.Users;
import com.sergey.githubusers.utils.CircleTransform;

import java.util.List;

/**
 * Created by sergey on 19.07.16.
 */
public class GitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<Users> users;
    private Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private boolean isLoading;
    private int threshold = 5;
    private int lastVisibleItem, totalItemCount;
    private OnLoadMoreListener onLoadMoreListener;
    private OnItemClicListener listener;



    public GitAdapter(List<Users> users, Context ctx, RecyclerView recyclerView, OnItemClicListener listener){
        this.users = users;
        context = ctx;
        this.listener = listener;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + threshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setOnLoadListener(OnLoadMoreListener onLoadListener){
        this.onLoadMoreListener = onLoadListener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_and_repo, parent, false);
            return new GitViewHolder(itemView);
        }else if(viewType == VIEW_TYPE_LOADING){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_item_layout, parent, false);
            return new LoadingViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GitViewHolder) {
            Users user = users.get(position);
            GitViewHolder gitViewHolder = (GitViewHolder) holder;
            gitViewHolder.name.setText(user.getLogin());
            gitViewHolder.score.setRating(user.getScore()/100*5);
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .centerCrop()
                    .override(60, 60)
                    .transform(new CircleTransform(context))
                    .placeholder(ResourcesCompat.getDrawable(context.getResources(), R.drawable.user_icon_placeholder, null))
                    .into(gitViewHolder.photo);
            ((GitViewHolder) holder).bind(listener,users.get(position));
        }else if(holder instanceof LoadingViewHolder){
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder)holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    @Override
    public int getItemViewType(int position) {
        return users.get(position)  == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setLoaded() {
        isLoading = false;
    }
    public void setUsers(List<Users> users){
        this.users = users;
    }
    public static class GitViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public RatingBar score;
        public ImageView photo;

        public GitViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            score = (RatingBar) itemView.findViewById(R.id.score);
            photo = (ImageView) itemView.findViewById(R.id.photo);

        }
        public void bind(OnItemClicListener listener, Users user){
                itemView.setOnClickListener((View v) -> listener.onItemClick(user));
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        LoadingViewHolder(View view){
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }
    }
}

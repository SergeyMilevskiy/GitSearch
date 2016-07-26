package com.sergey.githubusers.ui;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.sergey.githubusers.R;
import com.sergey.githubusers.network.NetworkManager;
import com.sergey.githubusers.pojo.User;
import com.sergey.githubusers.utils.CircleTransform;
import com.sergey.githubusers.utils.Utils;


import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ItemsActivity extends AppCompatActivity {

    private TextView name;
    private TextView followers;
    private ImageView foto;
    private RatingBar ratingBar;
    private String login;
    private NetworkManager networkManager;
    private float score;
    private CompositeSubscription compositeSubscription;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        networkManager = NetworkManager.getInstance(this);
        compositeSubscription = new CompositeSubscription();
        name = (TextView) findViewById(R.id.user_name);
        followers = (TextView) findViewById(R.id.followers);
        foto = (ImageView) findViewById(R.id.foto_img);
        ratingBar = (RatingBar) findViewById(R.id.rating);
        Intent intent = getIntent();
        login = intent.getStringExtra("user");
        score = intent.getFloatExtra("score", 0);

        /**
         * Here we're getting our observable from retrofit.
         * We use a login to get name and surname. If user not set them the field will be empty.
         */
        if(savedInstanceState == null) {
            Subscription subscibeuser = networkManager.requstUser(login)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(response -> Utils.errorHandler(response, ItemsActivity.this))
                    .map(Response::body)
                    .subscribe(ItemsActivity.this::setResult);
            compositeSubscription.add(subscibeuser);

        }
    }

    /**
     * Set result after requests.
     * @param user
     */

    private void setResult(User user){
        if(user.getName() != null){
            name.setText(user.getName());
        }
        int follower = user.getFollowers();
        followers.setText(String.format(getResources().getString(R.string.followers),follower));
        ratingBar.setRating(score/100*5);
        Glide.with(this)
                .load(user.getAvatarUrl())
                .centerCrop()
                .override(140, 140)
                .transform(new CircleTransform(this))
                .placeholder(ResourcesCompat.getDrawable(getResources(), R.drawable.user_icon_placeholder, null))
                .into(foto);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }
}

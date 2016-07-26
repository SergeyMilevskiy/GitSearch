package com.sergey.githubusers.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sergey.githubusers.GitAdapter;
import com.sergey.githubusers.pojo.User;
import com.sergey.githubusers.pojo.Users;
import com.sergey.githubusers.pojo.UsersRequst;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Network manager.
 */
public class NetworkManager {

    private static final String URL = "https://api.github.com/";
    private static NetworkManager instance;
    private GitHubAPI gitHubAPI;
    private  static final String PER_PAGE = "30";
    private CompositeSubscription compositeSubscription;
    private String search;
    private boolean flag = true;
    private ConnectivityManager cm;
    private static final String ORDER = "asc";


    private NetworkManager(Context ctx ){

        Retrofit retrofit = getRetrofit();
        gitHubAPI = retrofit.create(GitHubAPI.class);
        compositeSubscription = new CompositeSubscription();
        cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    public static NetworkManager getInstance(Context context){

        if(instance == null){
            instance = new NetworkManager(context);
        }
        return instance;

    }

    private Retrofit getRetrofit() {

        /**
         * Interceptor for debug purpose.
         * Of course we doesn't have to use on production it.
         */
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    public Observable<Response<UsersRequst>> requstSearch(String search){
        this.search = search;
        flag = true;
        return gitHubAPI.getSerarchResult(search, PER_PAGE, ORDER);
    }

    public Observable<Response<UsersRequst>> getNext(int page){
        return gitHubAPI.nextPage(search, PER_PAGE, String.valueOf(page));

    }

    /**
     * Check internet connections.
     * @return
     */

   public boolean isConnected(){
       NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
       return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());


   }

    public Observable<Response<User>> requstUser(String user){
        return gitHubAPI.getUser(user);
    }

}

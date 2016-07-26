package com.sergey.githubusers.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import com.jakewharton.rxbinding.widget.RxTextView;
import com.sergey.githubusers.GitAdapter;
import com.sergey.githubusers.OnLoadMoreListener;
import com.sergey.githubusers.R;

import com.sergey.githubusers.network.NetworkManager;
import com.sergey.githubusers.pojo.Users;
import com.sergey.githubusers.pojo.UsersRequst;
import com.sergey.githubusers.utils.Utils;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SearchActivity extends AppCompatActivity {

    private EditText searchField;
    private RecyclerView recyclerView;
    private List<Users> users = new ArrayList<>();
    private CompositeSubscription compositeSubscription;
    private NetworkManager networkManager;
    private GitAdapter gitAdapter;
    private String search;
    private static final int DELAY_TIME = 800;
    private static final int RETRY = 3;
    private static final int NUMBER_OF_SYMBOL = 3;
    private static int page = 2;
    private boolean flag = true;
    private InputMethodManager keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        networkManager  = NetworkManager.getInstance(this);
        compositeSubscription = new CompositeSubscription();
        searchField = (EditText) findViewById(R.id.search);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gitAdapter = new GitAdapter(users, this, recyclerView, SearchActivity.this::startItem);
        keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        /** Listener for lazy loading */
        gitAdapter.setOnLoadListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if(flag && checkNetwork()){
                    Subscription nextpage = networkManager.getNext(page)
                            .subscribeOn(Schedulers.newThread())
                            .doOnNext(response -> flag = response.isSuccessful())
                            .filter(response -> flag)
                            .map(Response::body)
                            .map(UsersRequst::getUsers)
                            .doOnNext(list -> flag = list.size() != 30 ? false : true)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(u -> setNext(u), Throwable::printStackTrace);
                    compositeSubscription.add(nextpage);
                }
            }
        });
        recyclerView.setAdapter(gitAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeSubscription = new CompositeSubscription();
        /**
         * Rx EditText thank you Jake Wharton. It makes my life easier.
         */

        Subscription editTextSub =
                RxTextView.textChanges(searchField)
                        .debounce(DELAY_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .filter(charSequence -> charSequence.length() >= NUMBER_OF_SYMBOL)
                        .map(CharSequence::toString)
                        .filter(currentSearch -> !currentSearch.equals(search))
                        .doOnNext(s -> page = 2)
                        .filter(s -> checkNetwork() )
                        .observeOn(Schedulers.newThread())
                        .flatMap(s -> networkManager.requstSearch(s))
                        .retry(RETRY)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(response -> Utils.errorHandler(response, SearchActivity.this))
                        .map(Response::body)
                        .map(UsersRequst::getUsers)
                        .doOnNext(list -> flag = list.size() != 30 ? false : true)
                        .subscribe(SearchActivity.this::setResult, Throwable::printStackTrace);

        compositeSubscription.add(editTextSub);

    }

    /**
     * Helper method where we set result to adapter after successful request.
     * @param list
     */

    private void setResult(List<Users> list){
        if(flag){
            list.add(null);
        }
        users = list;
        gitAdapter.setUsers(users);
        gitAdapter.notifyDataSetChanged();
        View view = this.getCurrentFocus();
        if(view != null) {
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Helper method for lazy loading
     * @param list
     */
    private void setNext(List<Users> list){
        int size = users.size();
        users.remove(size - 1);
        gitAdapter.notifyItemRemoved(size);
        users.addAll(list);
        if(flag){
            users.add(null);
        }
        gitAdapter.setUsers(users);
        gitAdapter.notifyDataSetChanged();
        gitAdapter.setLoaded();
    }

    /**
     * Helper method to invoke new activity.
     * @param user
     */
    private void startItem(Users user){
        Intent intent = new Intent(SearchActivity.this, ItemsActivity.class);
        intent.putExtra("user", user.getLogin());
        intent.putExtra("score", user.getScore());
        startActivity(intent);
    }

    /**
     * Check network before request.
     * @return
     */

    private boolean checkNetwork(){
        boolean check = networkManager.isConnected();
        if(!check){
            Toast.makeText(this, getText(R.string.no_internet), Toast.LENGTH_LONG).show();
            return check;
        }else {
            return check;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /**
         * Save search result for preventing unnecessary request if we returned from background.
         */
        search = searchField.getText().toString();
        if(compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       }

}

package com.sergey.githubusers.network;


import com.sergey.githubusers.pojo.User;
import com.sergey.githubusers.pojo.UsersRequst;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


public interface GitHubAPI {

    String API_SEARCH_USERS = "/search/users";
    String API_USER = "/users/" + "{user}";


    /**
     * Request for search
     * @param name
     * @param pages
     * @param sortorder
     * @return
     */

    @GET(API_SEARCH_USERS)
    Observable<Response<UsersRequst>> getSerarchResult (@Query("q") String name,
                                                        @Query("per_page") String pages,
                                                        @Query("order") String sortorder);

    /**
     * Request for single user.
     * @param user
     * @return
     */

    @GET(API_USER)
    Observable<Response<User>> getUser (@Path("user") String user);

    /**
     * Request per page.
     * @param name
     * @param pages
     * @param page
     * @return
     */

    @GET(API_SEARCH_USERS)
    Observable<Response<UsersRequst>> nextPage(@Query("q") String name,
                                     @Query("per_page") String pages,
                                     @Query("page") String page);


}

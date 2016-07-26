package com.sergey.githubusers.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;



public class UsersRequst {

    @SerializedName("items")
    @Expose
    private List<Users> items;
    @SerializedName("total_count")
    @Expose
    private Integer total_count;
    private Boolean incomplete_results;


    public void setUsersRequst(final List<Users> userses) {
        this.items = userses;

    }

    public List<Users> getUsers(){
        return items;
    }

    public Integer getTotalCount(){
        return total_count;
    }

    public Boolean getIncompleteResults(){
        return incomplete_results;
    }
}

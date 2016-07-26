package com.sergey.githubusers.utils;

import android.content.Context;
import android.widget.Toast;

import com.sergey.githubusers.R;

import retrofit2.Response;


public class Utils {

    public static boolean errorHandler(Response response, Context context){
        int code = response.code();
        if(code >= 400){
            Toast.makeText(context, context.getText(R.string.error),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}

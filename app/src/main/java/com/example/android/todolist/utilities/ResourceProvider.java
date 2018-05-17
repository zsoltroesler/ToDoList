package com.example.android.todolist.utilities;

import android.content.Context;

// This code snippet was provided by this post to be able to get string from SharedPreferences in
// MainViewModel class
// https://stackoverflow.com/questions/46666607/how-to-get-r-string-in-viewmodel-class-of-databinding-in-android
public class ResourceProvider {

    private Context mContext;

    public ResourceProvider(Context mContext) {
        this.mContext = mContext;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public String getString(int resId, String value) {
        return mContext.getString(resId, value);
    }
}
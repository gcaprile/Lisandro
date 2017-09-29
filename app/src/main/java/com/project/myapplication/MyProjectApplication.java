package com.project.myapplication;


import android.app.Application;

import io.realm.Realm;

public class MyProjectApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}

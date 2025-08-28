package com.example.todo.webapp.config;

public class DaggerBridge {
    private static final AppComponent appComponent = DaggerAppComponent.create();

    public static AppComponent getAppComponent() {
        return appComponent;
    }
}
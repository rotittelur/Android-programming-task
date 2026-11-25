package com.cbs.trivia.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static volatile VolleySingleton instance;
    private RequestQueue requestQueue;
    private final Context appContext;

    private VolleySingleton(Context context) {
        this.appContext = context.getApplicationContext();
        this.requestQueue = getRequestQueue();
    }

    public static VolleySingleton getInstance(Context context) {
        if (instance == null) {
            synchronized (VolleySingleton.class) {
                if (instance == null) {
                    instance = new VolleySingleton(context);
                }
            }
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appContext);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
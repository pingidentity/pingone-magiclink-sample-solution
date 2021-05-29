package com.pingidentity.magiclinkdemo;


import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CallUserInfoCallback implements Callback {
    private final Context context;
    private final Call nextCall;
    private final Callback nextCallback;
    private final AppCompatActivity activity;
    private final TextView welcomeView;

    private OkHttpClient client = new OkHttpClient();

    public CallUserInfoCallback(Context context, Call nextCall, Callback nextCallback, AppCompatActivity activity, TextView welcomeView)
    {
        this.context = context;
        this.nextCall = nextCall;
        this.nextCallback = nextCallback;
        this.activity = activity;
        this.welcomeView = welcomeView;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.d("userinfo-callback", "401 - Retrying userinfo call");
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            Log.d("userinfo-callback", "200 - Success");
            runSuccess(response);
            return;

            //setContentView(R.layout.activity_exchange_code);
        } else {
            // Request not successful
        }
    }

    private void runSuccess(Response response) throws IOException
    {
        Log.d("userinfo-callback", "200 - Success");
        String responseStr = response.body().string();

        String name = "";

        try {
            JSONObject responseJSON = new JSONObject(responseStr);
            if(responseJSON.has("given_name"))
                name = responseJSON.getString("given_name");

            if(name == null || name.trim().equals(""))
                name = responseJSON.getString("sub");

        }catch(JSONException e)
        {
            Log.e("exchangecode", "Error getting userinfo claims", e);
        }

        Log.d("exchangecode", "Userinfo name: " + name);

        activity.runOnUiThread(new CallUserInfoCallback.PrintWelcomeRunner(name));
    }

    class PrintWelcomeRunner implements Runnable
    {
        final String name;

        PrintWelcomeRunner(String name)
        {
            this.name = name;

        }

        public void run() {
            Log.i("exchangecode", String.format("Welcome %s", name));
            welcomeView.setText(String.format("Welcome %s", name));
        }
    }
}

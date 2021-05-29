package com.pingidentity.magiclinkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ExchangeCode extends AppCompatActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();

    protected TextView welcomeView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_code);

        Bundle b = getIntent().getExtras();
        String code = getIntent().getData().getQueryParameter("code");

        this.welcomeView = findViewById(R.id.welcome);

        Callback nextCallback = new CallUserInfoCallback(getApplicationContext(), null, null, this, welcomeView);

        OIDCTools.exchangeCode(client, getApplicationContext(), code, nextCallback, null);
    }

}
package com.pingidentity.magiclinkdemo.ui.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.pingidentity.magiclinkdemo.oidc.OIDCTools;
import com.pingidentity.magiclinkdemo.R;
import com.pingidentity.magiclinkdemo.ui.view.callback.WaitMagicLinkCallback;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

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

        Callback nextCallback = new WaitMagicLinkCallback(getApplicationContext(), null, null, this, welcomeView);

        OIDCTools.exchangeCode(client, getApplicationContext(), code, nextCallback, null);
    }

}
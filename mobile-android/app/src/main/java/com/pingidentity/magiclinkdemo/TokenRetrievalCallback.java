package com.pingidentity.magiclinkdemo;


import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class TokenRetrievalCallback implements Callback {
    private final Context context;
    private final Callback nextCallback;

    private OkHttpClient client = new OkHttpClient();

    public TokenRetrievalCallback(Context context, Callback nextCallback)
    {
        this.context = context;
        this.nextCallback = nextCallback;

    }

    @Override
    public void onFailure(Call call, IOException e) {
        // Something went wrong
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String responseStr = response.body().string();

            OIDCTools.saveTokens(context, responseStr);

            Call nextCall = OIDCTools.callGET(client, context, BuildConfig.OIDC_USERINFO, this.nextCallback);

            return;

            //setContentView(R.layout.activity_exchange_code);
        } else {
            // Request not successful
        }
    }
}

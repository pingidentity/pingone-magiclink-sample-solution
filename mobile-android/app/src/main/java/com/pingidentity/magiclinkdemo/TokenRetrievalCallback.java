package com.pingidentity.magiclinkdemo;


import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TokenRetrievalCallback implements Callback {
    private final Context context;
    private final Call nextCall;
    private final Callback nextCallback;

    public TokenRetrievalCallback(Context context, Call nextCall, Callback nextCallback)
    {
        this.context = context;
        this.nextCall = nextCall;
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

            this.nextCall.enqueue(this.nextCallback);

            return;

            //setContentView(R.layout.activity_exchange_code);
        } else {
            // Request not successful
        }
    }
}

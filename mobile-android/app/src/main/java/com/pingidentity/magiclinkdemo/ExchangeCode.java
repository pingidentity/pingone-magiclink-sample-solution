package com.pingidentity.magiclinkdemo;

import androidx.appcompat.app.AppCompatActivity;

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

    private TextView welcomeView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_code);

        Bundle b = getIntent().getExtras();
        String code = getIntent().getData().getQueryParameter("code");

        this.welcomeView = findViewById(R.id.welcome);

        Call nextCall = OIDCTools.callGET(client, getApplicationContext(), BuildConfig.OIDC_USERINFO, null);
        Callback nextCallback = getUserInfoCallback();

        OIDCTools.exchangeCode(client, getApplicationContext(), code, nextCall, nextCallback, null);
    }

    private Callback getUserInfoCallback()
    {
        Callback userInfoCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 401)
                {
                    Log.i("userinfo-callback", "401 - Unauthorized, AT: " + SecureStorage.getValue(getApplicationContext(), SecureStorage.TOKEN_ACCESS_TOKEN));
                    Call nextCall = OIDCTools.callGET(client, getApplicationContext(), BuildConfig.OIDC_USERINFO, null);
                    Callback userInfoCallback = getUserInfoCallback();

                    //TODO implement error callback
                    Callback errorCallback = null;

                    OIDCTools.refreshToken(client, getApplicationContext(), nextCall, userInfoCallback, errorCallback);
                }
                else if (response.isSuccessful()) {
                    Log.i("userinfo-callback", "200 - Success");
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

                    }
                    runOnUiThread(new PrintWelcomeRunner(name));

                } else {
                    // Request not successful
                }
            }
        };

        return userInfoCallback;
    }

    class PrintWelcomeRunner implements Runnable
    {
        final String name;

        PrintWelcomeRunner(String name)
        {
            this.name = name;

        }

        public void run() {
            welcomeView.setText(String.format("Welcome %s", name));
        }
    }
}
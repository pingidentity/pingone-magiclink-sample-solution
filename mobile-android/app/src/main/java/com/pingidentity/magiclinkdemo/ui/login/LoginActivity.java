package com.pingidentity.magiclinkdemo.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pingidentity.magiclinkdemo.BuildConfig;
import com.pingidentity.magiclinkdemo.OIDCTools;
import com.pingidentity.magiclinkdemo.R;
import com.pingidentity.magiclinkdemo.SecureStorage;
import com.pingidentity.magiclinkdemo.WaitMagicLink;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                //setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {

        initiateMagicLink();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private String getEmailAddress()
    {
        EditText usernameText = (EditText) findViewById(R.id.username);
        String email = usernameText.getText().toString();

        return email;
    }

    private void initiateMagicLink()
    {
        SecureStorage.clearValues(getApplicationContext());

        JSONObject jsonObject = new JSONObject();
        try {


            String nonce = UUID.randomUUID().toString();
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_NONCE, nonce);

            String state = UUID.randomUUID().toString();
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_STATE, state);

            String email = getEmailAddress();
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_EXPECTED_EMAIL, email);

            String clientId = BuildConfig.CLIENT_ID;
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_CLIENT_ID, clientId);

            String redirectUri = BuildConfig.CLIENT_CALLBACK;
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_REDIRECT_URI, redirectUri);

            String codeVerifier = OIDCTools.generateCodeVerifier();
            SecureStorage.setValue(getApplicationContext(), SecureStorage.LOGIN_CODE_VERIFIER, codeVerifier);

            String codeChallenge = OIDCTools.generateCodeChallange(codeVerifier);

            jsonObject.put("subject", email);
            jsonObject.put("nonce", nonce);
            jsonObject.put("code_challenge_method", "S256");
            jsonObject.put("code_challenge", codeChallenge);
            jsonObject.put("scope", BuildConfig.CLIENT_SCOPES);
            jsonObject.put("client_id", clientId);
            jsonObject.put("state", state);
            jsonObject.put("redirect_uri", redirectUri);

        }catch(JSONException e)
        {
            Log.e("initiateMagicLink", "Failed generating jsonObject");
            return;
        }

        String postBodyContent = jsonObject.toString();
        RequestBody postBody = RequestBody.create(postBodyContent.getBytes());


        Request.Builder requestBuilder = new Request.Builder().url(BuildConfig.MAGICLINK_BASEURL + "/pingone/register")
                .addHeader("Content-Type", "application/json")
                .post(postBody);

        Request request = requestBuilder.build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();

                    Intent intent = new Intent(LoginActivity.this, WaitMagicLink.class);

                    String email = getEmailAddress();

                    SecureStorage.setValue(getApplicationContext(), SecureStorage.INPUT_EMAIL, email);

                    startActivity(intent);

                } else {
                    // Request not successful
                }
            }
        };

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
}

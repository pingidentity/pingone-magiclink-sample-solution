package com.pingidentity.magiclinkdemo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OIDCTools {

    public synchronized static boolean refreshToken(OkHttpClient client, Context context, Call nextCall, Callback nextCallback, Callback errorCallback)
    {
        String refreshToken = SecureStorage.getValue(context, SecureStorage.TOKEN_REFRESH_TOKEN);

        if(refreshToken == null || refreshToken.trim().equals(""))
        {
            Log.i("oidc-tools", "Refreshing token failed - no refresh token found");
            return false;
        }

        String accessToken = SecureStorage.getValue(context, SecureStorage.TOKEN_ACCESS_TOKEN);

        //only refresh if accessToken is expiring
        if(accessToken != null && accessToken.split("\\.").length == 2)
        {
            String json = new String(Base64.getDecoder().decode(accessToken.split("\\.")[1]));
            try {
                JSONObject jsonObject = new JSONObject(json);

                long exp = jsonObject.getLong("exp");
                long now = (Instant.now().getEpochSecond() + BuildConfig.OIDC_TOKEN_EXP_TOLERANCE ); //adding second buffer

                if(now < exp)
                {
                    Log.d("oidc-tools", "Access token hasn't expired yet. Not refreshing.");
                    return true;
                }
            } catch (JSONException e) {
                Log.e("oidc-tools", "Unable to unwrap Access Token JSON, continuing with refresh", e);
            }
        }

        //Instant.now().getEpochSecond()

        Log.i("oidc-tools", "Refreshing tokens");

        SecureStorage.removeValue(context, SecureStorage.TOKEN_ACCESS_TOKEN);

        String clientId = SecureStorage.getValue(context, SecureStorage.LOGIN_CLIENT_ID);
        String redirectUri = SecureStorage.getValue(context, SecureStorage.LOGIN_REDIRECT_URI);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("grant_type", "refresh_token");
        formBuilder.add("client_id", clientId);
        formBuilder.add("redirect_uri", redirectUri);
        formBuilder.add("refresh_token", refreshToken);

        RequestBody postBody = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(BuildConfig.OIDC_TOKEN)
                .post(postBody);

        TokenRetrievalCallback tokenRetrievalCallback = new TokenRetrievalCallback(context, nextCallback);

        post(client, BuildConfig.OIDC_TOKEN, postBody, tokenRetrievalCallback);

        return true;
    }

    public synchronized static String getAccessToken(OkHttpClient client, Context context)
    {
        //in case token is being refreshed, we will wait until a new Access Token is available

        String accessToken = SecureStorage.getValue(context, SecureStorage.TOKEN_ACCESS_TOKEN);

        int count = 0;

        while(accessToken == null && count < 50)
        {
            try {
                Log.d("oidc-tools", "Waiting for access token");
                Thread.sleep(100);
                accessToken = SecureStorage.getValue(context, SecureStorage.TOKEN_ACCESS_TOKEN);
            }catch(InterruptedException e)
            {}

            count++;
        }

        return accessToken;
    }

    public synchronized static boolean exchangeCode(OkHttpClient client, Context context, String code, Callback nextCallback, Callback errorCallback)
    {
        Log.d("oidc-tools", "Exchanging code for access token");

        String clientId = SecureStorage.getValue(context, SecureStorage.LOGIN_CLIENT_ID);
        String codeVerifier = SecureStorage.getValue(context, SecureStorage.LOGIN_CODE_VERIFIER);
        String redirectUri = SecureStorage.getValue(context, SecureStorage.LOGIN_REDIRECT_URI);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("grant_type", "authorization_code");
        formBuilder.add("client_id", clientId);
        formBuilder.add("code_verifier", codeVerifier);
        formBuilder.add("code", code);
        formBuilder.add("redirect_uri", redirectUri);

        RequestBody postBody = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(BuildConfig.OIDC_TOKEN)
                .post(postBody);

        TokenRetrievalCallback tokenRetrievalCallback = new TokenRetrievalCallback(context, nextCallback);

        post(client, BuildConfig.OIDC_TOKEN, postBody, tokenRetrievalCallback);

        return true;
    }

    private static Call post(OkHttpClient client, String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);

        if(callback != null)
            call.enqueue(callback);

        return call;
    }

    public static Call callGET(OkHttpClient client, Context context, String url, Callback callback) {

        String accessToken = getAccessToken(client, context);

        String safeAT = (accessToken != null) ? accessToken.substring(0, accessToken.lastIndexOf(".")): "no-at";

        Log.d("oidc-tools", String.format("Calling %s with access token %s (signature removed)", url, safeAT));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        Call call = client.newCall(request);

        if(callback != null)
            call.enqueue(callback);

        return call;
    }


    public static JSONObject saveTokens(@NonNull Context context, String accessTokenResponse)
    {
        Log.d("oidc-tools", "Saving tokens");

        String expectedNonce = SecureStorage.getValue(context, SecureStorage.LOGIN_NONCE, true);

        if(expectedNonce == null || expectedNonce.trim().equals(""))
            throw new OIDCException("Nonce value has not been set");

        JSONObject jsonObject;
        try
        {
            jsonObject = new JSONObject(accessTokenResponse);

            if(jsonObject == null)
                throw new OIDCException("Parsing accessTokenResponse returned null");

        } catch(JSONException e)
        {
            throw new OIDCException("Could not parse access token response: " + accessTokenResponse, e);
        }

        if(!jsonObject.has("access_token"))
            throw new OIDCException("Expecting access_token claim");
        else if(!jsonObject.has("id_token"))
            throw new OIDCException("Expecting id_token claim");

        String accessToken, idToken;

        try {
            accessToken = jsonObject.getString("access_token");
            idToken = jsonObject.getString("id_token");
        } catch(JSONException e)
        {
            throw new OIDCException("Issue getting access token or id token", e);
        }

        //TODO: make jwks configurable
        JwtClaims verifiedIdToken = getVerifiedClaims(idToken, BuildConfig.OIDC_JWKS);

        if(verifiedIdToken == null)
            throw new OIDCException("ID Token result is null after verification");

        try {
            if (!verifiedIdToken.hasClaim("nonce"))
                throw new OIDCException("ID Token nonce is not available");
            else if (!verifiedIdToken.getStringClaimValue("nonce").equalsIgnoreCase(expectedNonce))
                throw new OIDCException("ID Token nonce is not expected");
        }catch(MalformedClaimException e)
        {
            throw new OIDCException("Malformed nonce value", e);
        }

        SecureStorage.setValue(context, SecureStorage.TOKEN_ACCESS_TOKEN, accessToken);
        SecureStorage.setValue(context, SecureStorage.TOKEN_ID_TOKEN, idToken);

        Log.d("oidc-tools", "Tokens saved");

        if(jsonObject.has("refresh_token"))
        {
            Log.d("oidc-tools", "Saving refresh token");
            try {
                SecureStorage.setValue(context, SecureStorage.TOKEN_REFRESH_TOKEN, jsonObject.getString("refresh_token"));
            }catch(JSONException e)
            {
            }
        }
        else
            Log.d("oidc-tools", "No refresh token found");

        return jsonObject;
    }

    public static JwtClaims getVerifiedClaims(String jwt, String jwks)
    {
        JwtConsumer jwtConsumer = getJwtConsumer(jwks);

        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);

            return jwtClaims;
        } catch(Exception e)
        {
            throw new OIDCException("Unable to verify jwt: " + jwt, e);
        }
    }

    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);

    }

    public static String generateCodeChallange(String codeVerifier) {
        byte[] bytes;

        try {
            bytes = codeVerifier.getBytes("US-ASCII");
        }catch(UnsupportedEncodingException e)
        {
            throw new OIDCException("Could not encode bytes", e);
        }

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }catch(NoSuchAlgorithmException e)
        {
            throw new OIDCException("SHA-256 algorithm does not exist", e);
        }

        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static JwtConsumer getJwtConsumer(String jwksUrl) {
        HttpsJwks httpsJwks = new HttpsJwks(jwksUrl);
        VerificationKeyResolver resolver = new HttpsJwksVerificationKeyResolver(httpsJwks);
        return new JwtConsumerBuilder()// Ensure expected issuer
                .setVerificationKeyResolver(resolver) // Verify the signature
                .setSkipDefaultAudienceValidation()
                .build();
    }

}

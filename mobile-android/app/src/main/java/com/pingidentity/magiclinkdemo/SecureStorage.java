package com.pingidentity.magiclinkdemo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class SecureStorage {

    public static final String INPUT_EMAIL = "com.pingidentity.magiclink.values.INPUT_EMAIL";
    public static final String LOGIN_NONCE = "com.pingidentity.magiclink.values.LOGIN_NONCE";
    public static final String LOGIN_CODE_VERIFIER = "com.pingidentity.magiclink.values.LOGIN_CODE_VERIFIER";
    public static final String LOGIN_STATE = "com.pingidentity.magiclink.values.LOGIN_STATE";
    public static final String LOGIN_EXPECTED_EMAIL = "com.pingidentity.magiclink.values.LOGIN_EXPECTED_EMAIL";
    public static final String LOGIN_CLIENT_ID = "com.pingidentity.magiclink.values.LOGIN_CLIENT_ID";
    public static final String LOGIN_REDIRECT_URI = "com.pingidentity.magiclink.values.LOGIN_REDIRECT_URI";
    public static final String TOKEN_ACCESS_TOKEN = "com.pingidentity.magiclink.values.TOKEN_ACCESS_TOKEN";
    public static final String TOKEN_ID_TOKEN = "com.pingidentity.magiclink.values.TOKEN_ID_TOKEN";
    public static final String TOKEN_REFRESH_TOKEN = "com.pingidentity.magiclink.values.TOKEN_REFRESH_TOKEN";

    public static void setValue(@NonNull Context context, String name, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static String getValue(@NonNull Context context, String name) {
        return getValue(context, name, false);
    }

    public static String getValue(@NonNull Context context, String name, boolean removeValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);

        String returnValue = sharedPreferences.getString(name, null);

        if(removeValue)
            removeValue(context, name);

        return returnValue;
    }

    public static void clearValues(@NonNull Context context)
    {
        removeValue(context, INPUT_EMAIL);
        removeValue(context, LOGIN_NONCE);
        removeValue(context, LOGIN_CODE_VERIFIER);
        removeValue(context, LOGIN_EXPECTED_EMAIL);
        removeValue(context, LOGIN_CLIENT_ID);
        removeValue(context, LOGIN_REDIRECT_URI);
        removeValue(context, TOKEN_ACCESS_TOKEN);
        removeValue(context, TOKEN_ID_TOKEN);
        removeValue(context, TOKEN_REFRESH_TOKEN);
    }

    public static void removeValue(@NonNull Context context, String name)
    {
        context.deleteSharedPreferences(name);
    }
}

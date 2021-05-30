package com.pingidentity.magiclinkdemo.ui.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.pingidentity.magiclinkdemo.R;
import com.pingidentity.magiclinkdemo.SecureStorage;
import com.pingidentity.magiclinkdemo.ui.login.LoginActivity;

public class WaitMagicLink extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_magic_link);

        Intent intent = getIntent();
        String email = SecureStorage.getValue(getApplicationContext(), SecureStorage.INPUT_EMAIL);

        TextView textView = findViewById(R.id.emailView);
        textView.setText(String.format("Using this device, please check your email (%s) and click on the magic link. ", email));
    }
}
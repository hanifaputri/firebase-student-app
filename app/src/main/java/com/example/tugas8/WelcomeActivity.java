package com.example.tugas8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    TextView tv_welcome;
    Button btnLogin, btnRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Hide action bar
        getSupportActionBar().hide();
        tv_welcome = findViewById(R.id.tv_welcome);
        btnLogin = (Button) findViewById(R.id.btn_wc_login);
        btnRegister = (Button) findViewById(R.id.btn_wc_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user already authenticated or not
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        if (user != null) {
            Intent home = new Intent(WelcomeActivity.this, ListActivity.class);
            startActivity(home);
            finish();
        }
    }
}
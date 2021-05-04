package com.example.tugas8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView tv_login;
    TextInputLayout tx_email, tx_pass, tx_rePass;
    Button btnRegister;
    ImageView btnBack;

    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide action bar
        getSupportActionBar().hide();

        tx_email = findViewById(R.id.tx_reg_email);
        tx_pass = findViewById(R.id.tx_reg_password);
        tx_rePass = findViewById(R.id.tx_reg_repass);
        btnRegister = findViewById(R.id.btn_reg_email);
        btnBack = findViewById(R.id.btn_reg_back);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        tv_login = findViewById(R.id.tv_reg_login);
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
    }

    private void createUser() {
        // Create user
        String email = tx_email.getEditText().getText().toString();
        String pass = tx_pass.getEditText().getText().toString();

        tx_email.setError(null);
        tx_pass.setError(null);

        if (!email.isEmpty() && !pass.isEmpty()) {
            if (validateEmail(email) && validatePassword(pass)){
                // [START create_user_with_email]
                btnRegister.setEnabled(false);
                btnRegister.setText("Please wait ...");
                mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                finish();
//                                Toast.makeText(RegisterActivity.this, "Register successs", Toast.LENGTH_SHORT).show();
                            } else {
                                btnRegister.setEnabled(true);
                                btnRegister.setText("Register");
                                Log.w(TAG, "createUserWithEmail:failure" + task.getException());
                                Toast.makeText(RegisterActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            } else if (!validateEmail(email)){
                tx_email.setError("Please enter a valid email address");
            } else {
                tx_pass.setError("Password must contain at least 6 character");
            }
        } else if (email.isEmpty()){
            tx_email.setError("Please enter email address");
        } else {
            tx_pass.setError("Please enter the password");
        }
    }

    public boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean validatePassword(String pass) {
        return pass.length() > 5;
    }

}
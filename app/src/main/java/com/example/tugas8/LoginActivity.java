package com.example.tugas8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button btnLogin;
    ImageView btnBack;
    TextView btnRegister, btnReset;
    TextInputLayout tx_email, tx_pass;

    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide action bar
        getSupportActionBar().hide();

        tx_email = (TextInputLayout) findViewById(R.id.tx_login_email);
        tx_pass = (TextInputLayout) findViewById(R.id.tx_login_password);
        btnLogin = (Button) findViewById(R.id.btn_login_email);
        btnReset = (TextView) findViewById(R.id.tv_reset_pass);
        btnRegister = (TextView) findViewById(R.id.tv_login_register);
        btnBack = (ImageView) findViewById(R.id.btn_login_back);

        btnRegister = findViewById(R.id.tv_login_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(register, 9);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset = new Intent(LoginActivity.this, ResetActivity.class);
                startActivityForResult(reset, 10);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 9 && resultCode == RESULT_OK){
            Toast.makeText(this, "Register success! Please log in.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 10 && resultCode == RESULT_OK) {
            String resEmail = data.getStringExtra("email");
            Toast.makeText(this, "Reset instruction has been sent to email to " + resEmail, Toast.LENGTH_LONG).show();
        }
    }

    private void loginUser() {
        // Create user
        String email = tx_email.getEditText().getText().toString();
        String pass = tx_pass.getEditText().getText().toString();

        tx_email.setError(null);
        tx_pass.setError(null);

        if (!email.isEmpty() && !pass.isEmpty()) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Authenticating ...");
            if (validateEmail(email)){
                mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                // Redirect to homepage
                                Intent home = new Intent(LoginActivity.this, ListActivity.class);
                                startActivity(home);
                                finish();
                            } else {
                                btnLogin.setEnabled(true);
                                btnLogin.setText("Login");
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure: " + task.getException());
                                Toast.makeText(LoginActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                ;
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
}
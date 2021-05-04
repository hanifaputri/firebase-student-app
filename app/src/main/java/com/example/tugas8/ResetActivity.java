package com.example.tugas8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {
    TextInputLayout tx_email;
    Button btnReset;
    ImageView btnBack;

    FirebaseAuth mAuth;
    private static final String TAG = "Reset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        // Hide action bar
        getSupportActionBar().hide();

        tx_email = (TextInputLayout) findViewById(R.id.tx_reset_email);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnBack = (ImageView) findViewById(R.id.btn_reset_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetEmail();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
    }

    private void resetEmail() {
        tx_email.setError(null);
        String email = tx_email.getEditText().getText().toString().trim();
        // Validate email
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.isEmpty()) {
            btnReset.setEnabled(false);
            btnReset.setText("Sending Email");
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("email", email);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        btnReset.setEnabled(true);
                        btnReset.setText("Reset Password");
                        Toast.makeText(ResetActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, task.getException().getMessage());
                    }
                }
            });
        } else {
            btnReset.setEnabled(true);
            btnReset.setText("Reset Password");
            tx_email.setError("Email address not valid");
        }
    }
}
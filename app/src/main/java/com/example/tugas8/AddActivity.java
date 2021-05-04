package com.example.tugas8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddActivity extends AppCompatActivity {
    LinearLayout mainLayout;
    TextInputLayout nama, nim, kelas;
    AutoCompleteTextView optionProdi;
    Button btnAddData, btnAddImg, btnClearImg;
    ImageView imgProfile;
    private static final String TAG = "Add";

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference ref = db.getReference("student");
    private StorageReference storage = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private User data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mainLayout = (LinearLayout) findViewById(R.id.ll_add_activity);
        nama = (TextInputLayout)findViewById(R.id.tx_add_name);
        nim = (TextInputLayout) findViewById(R.id.tx_add_nim);
        kelas = (TextInputLayout) findViewById(R.id.tx_add_class);
        optionProdi = (AutoCompleteTextView) findViewById(R.id.ac_add_prodi);
        imgProfile = (ImageView) findViewById(R.id.img_add);

        //Authenticate anonymously
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.i(TAG, "User authenticated");
        } else {
            finish();
//            signInAnonymously();
        }

        btnAddData = (Button) findViewById(R.id.btn_add);
        btnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
            }
        });

        // Define user model
        data = new User();

        btnAddImg = (Button) findViewById(R.id.btn_add_img);
        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, 2);
            }
        });

        btnClearImg = (Button) findViewById(R.id.btn_clear_img);
        btnClearImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImg();
            }
        });

        // Dropdown
        String[] prodi = {"Teknologi Informasi", "Sistem Informasi", "Teknik Informatika", "Teknik Komputer", "PTI"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(AddActivity.this, R.layout.list_prodi, prodi);
        optionProdi.setText(arrayAdapter.getItem(0).toString(), false);
        optionProdi.setAdapter(arrayAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check action from gallery
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            //Show button & change text
            btnAddImg.setText("Change Photo");
            btnClearImg.setVisibility(View.VISIBLE);

            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }
    
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "signInAnonymously:FAILURE", exception);
                }
            });
    }

    private boolean isEmpty(TextInputLayout textInput) {
        return textInput.getEditText().getText().toString().trim().length() == 0;
    }

    private void addData(){
        if (!isEmpty(nama) && !isEmpty(kelas) && !isEmpty(nim)) {
//             Check existing data
            ref.orderByChild("nim").equalTo(nim.getEditText().getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        nim.setError("Nim already exist");
                    } else {
                        String value = optionProdi.getText().toString() + " " + kelas.getEditText().getText().toString().toUpperCase();

                        data.setNama(nama.getEditText().getText().toString().trim());
                        data.setNim(nim.getEditText().getText().toString().trim());
                        data.setKelas(value.trim());

                        // Push image only if photo is set
                        if (imageUri != null) {
                            pushImage();
                        } else {
                            pushData();
                        }
                        nim.setError(null);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, error.toException());
                }
            });
        }
        else {
            Toast.makeText(AddActivity.this, "Please fill the form", Toast.LENGTH_SHORT).show();
        }
    }

    private void pushImage() {
        StorageReference imgRef = storage.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        imgRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        btnAddData.setEnabled(true);
                        btnAddData.setText("Add Data");

                        String imgUrl = uri.toString();
                        data.setImageUrl(imgUrl);

                        pushData();
                      }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                btnAddData.setText("Loading ...");
                btnAddData.setEnabled(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, e.getMessage());
                Toast.makeText(AddActivity.this, "Upload failed: " + e, Toast.LENGTH_SHORT).show();

                btnAddData.setEnabled(true);
                btnAddData.setText("Add Data");
            }
        });
    }

    private void pushData() {
        // Push user data
        ref.push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                clearImg();
                nama.getEditText().setText("");
                nim.getEditText().setText("");
                kelas.getEditText().setText("");

                Snackbar.make(mainLayout, "Data has successfully added", Snackbar.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, e.getMessage());
                Toast.makeText(AddActivity.this, "Data failed to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void clearImg() {
        imageUri = null;
        imgProfile.setImageURI(null);

        btnClearImg.setVisibility(View.GONE);
        btnAddImg.setText("Add Photo");
    }
}
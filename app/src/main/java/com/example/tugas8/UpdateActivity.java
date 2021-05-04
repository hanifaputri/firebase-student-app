package com.example.tugas8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

public class UpdateActivity extends AppCompatActivity {
    TextInputLayout tx_nama, tx_nim, tx_kelas;
    AutoCompleteTextView optionProdi;
    Button btnUpdate, btnDelete, btnAddImg, btnClearImg;
    String nama, nim, kelas, url, key, prodi, kls;
    Boolean isPhotoChanged;
    ImageView img;

    private static final String TAG = "Edit Data";

    // Firebase instance start
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference ref = db.getReference("student");
    private StorageReference storage = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private User data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        tx_nama = (TextInputLayout) findViewById(R.id.tx_edit_name);
        tx_nim = (TextInputLayout) findViewById(R.id.tx_edit_nim);
        tx_kelas = (TextInputLayout) findViewById(R.id.tx_edit_class);
        img = (ImageView) findViewById(R.id.img_edit);
        optionProdi = (AutoCompleteTextView) findViewById(R.id.ac_edit_prodi);
        btnUpdate = (Button) findViewById(R.id.btn_edit_update);
        btnDelete = (Button) findViewById(R.id.btn_edit_delete);
        btnAddImg = (Button) findViewById(R.id.btn_edit_add_img);
        btnClearImg = (Button) findViewById(R.id.btn_edit_clear_img);

        // Define user model
        data = new User();
        // Retrieve intent data
        getIntentData();
        // Retrieve key from database
        loadDataKey();

        // Dropdown
        String[] prodi = {"Teknologi Informasi", "Sistem Informasi", "Teknik Informatika", "Teknik Komputer", "PTI"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(UpdateActivity.this, R.layout.list_prodi, prodi);
        optionProdi.setAdapter(arrayAdapter);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });

        // Button for image
        btnAddImg = (Button) findViewById(R.id.btn_edit_add_img);
        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, 3);
            }
        });

        btnClearImg = (Button) findViewById(R.id.btn_edit_clear_img);
        btnClearImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImg();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.i(TAG, "User authenticated");
        } else {
            // For test only
//            signInAnonymously();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check action from gallery
        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            // Flag changes
            isPhotoChanged = true;

            //Show button & change text
            btnAddImg.setText("Change Photo");
            btnClearImg.setVisibility(View.VISIBLE);

            imageUri = data.getData();
            img.setImageURI(imageUri);
        }
    }

    private void getIntentData(){
        if(getIntent().hasExtra("img") && getIntent().hasExtra("nama") && getIntent().hasExtra("nim") && getIntent().hasExtra("kelas")) {
            nama = getIntent().getStringExtra("nama");
            nim = getIntent().getStringExtra("nim");
            kelas = getIntent().getStringExtra("kelas");
            url = getIntent().getStringExtra("img");

            // Substract data
            int totalChar = kelas.length();
            prodi = kelas.substring(0, totalChar-2).trim();
            kls = kelas.substring(totalChar-2, totalChar).trim();

            tx_nama.getEditText().setText(nama);
            tx_nim.getEditText().setText(nim);
            tx_kelas.getEditText().setText(kls);
            optionProdi.setText(prodi, false);

            resetImg();
        } else {
            Log.w(TAG, "No intent data retrieved.");
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
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
    
    private void updateData(){
        if (!isEmpty(tx_nama) && !isEmpty(tx_kelas) && !isEmpty(tx_nim)) {
            // If NIM change, check avaibility first
            if (!tx_nim.getEditText().getText().toString().equals(nim)){
                ref.orderByChild("nim").equalTo(tx_nim.getEditText().getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            pushData();
                        } else {
                            tx_nim.setError("NIM already exists");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, error.getMessage());
                    }
                });
            } else {
                pushData();
            }
        }
        else {
            Toast.makeText(UpdateActivity.this, "Please fill the form", Toast.LENGTH_SHORT).show();
        }
    }

    private void pushData() {
        String value = optionProdi.getText().toString() + " " + tx_kelas.getEditText().getText().toString().toUpperCase();

        data.setNama(tx_nama.getEditText().getText().toString().trim());
        data.setNim(tx_nim.getEditText().getText().toString().trim());
        data.setKelas(value.trim());

        // Push image only if photo is changed
        if (isPhotoChanged != null && imageUri != null) {
            pushImage();
        } else {
            data.setImageUrl(url);

            // Update data
            ref.child(key).setValue(data);
            setResult(990);
            finish();
        }
    }
    
    private void loadDataKey() {
        // Find snapshot id based on nim
        ref.orderByChild("nim").equalTo(nim).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    key = ds.getKey();
                    Log.d(TAG,"Key: " + key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }
    
    private void deletePrevImage() {
        // Remove previous image if image not the same as placeholder
        Log.i(TAG, "URL: " + url + " \nDef URL:" + data.getDefaultImageUrl() + "\nEqual? :" + (url.equals(data.getDefaultImageUrl())));
        if (!url.equals(data.getDefaultImageUrl())) {
            StorageReference prevRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            prevRef.delete();

            Log.i(TAG, "Image is not placeholder. Previous image deleted");
        } else {
            Log.i(TAG, "Image placeholder. Previous image not deleted.");
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
                        btnDelete.setEnabled(true);
                        btnUpdate.setEnabled(true);
                        btnUpdate.setText("Update");
                        String imgUrl = uri.toString();

                        // Delete previous image
                        deletePrevImage();

                        // Update data url
                        data.setImageUrl(imgUrl);

                        ref.child(key).setValue(data);
                        setResult(990);
                        finish();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                btnUpdate.setText("Loading ...");
                btnDelete.setEnabled(false);
                btnUpdate.setEnabled(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnDelete.setEnabled(true);
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Update");
                Toast.makeText(UpdateActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    private void resetImg() {
        isPhotoChanged = false;
        imageUri = null;

        Glide.with(UpdateActivity.this)
                .load(url)
                .fitCenter()
                .dontAnimate()
                .dontTransform()
                .into(img);

        btnClearImg.setVisibility(View.GONE);
        btnAddImg.setText("Change photo");
    }

    private void deleteData() {
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Data?");
        builder.setMessage("Are you sure you want to delete " + nama + " data? The process can't be undone");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ref.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Delete previous image from storage
                        deletePrevImage();
                        // Finish task and return result code
                        setResult(991);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, e.getMessage());
                        Toast.makeText(UpdateActivity.this, "Data failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
}
package com.example.tugas8;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String nama, nim, kelas, imageUrl;

    public User(){
        // Set default image URL
        this.imageUrl = getDefaultImageUrl();
    }

    public User(String nama, String nim, String kelas, String imageUrl){
        this.nama = nama;
        this.nim = nim;
        this.kelas = kelas;
        this.imageUrl = imageUrl;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Exclude default url as object instance in db
    @Exclude
    public String getDefaultImageUrl () {
        return "https://firebasestorage.googleapis.com/v0/b/tugas8-39632.appspot.com/o/ava_placeholder.jpeg?alt=media&token=bb05bc4f-f706-4c72-9915-82d42ca3cb6c";
    }
}

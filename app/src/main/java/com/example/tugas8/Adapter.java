package com.example.tugas8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private Context context;
    private Activity activity;
    private ArrayList<User> studentList;
    private Animation translateAnim;
//    private List<User> studentListAll;

    private int position;

    public Adapter(Activity activity, Context context, ArrayList<User> studentList){
        this.activity = activity;
        this.context = context;
        this.studentList = studentList;

//        // Duplicate all data
//        this.studentListAll = studentList;
//        Log.i("Size array 1", "" + studentList.size());
//
//        Log.i("Size", "" + studentListAll.size());

//        for (int i = 0; i < studentListAll.size(); i++){
//            Log.i("TAG", "" + studentListAll.get(i).getNama());
//        }
    }
//
//    // Filter
//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence charSequence) {
//                List<User> filteredList = new ArrayList<>();
//                CharSequence in = charSequence.toString().toLowerCase();
//
//                if (charSequence.toString().isEmpty()) {
//                    filteredList.addAll(studentList);
//                } else {
//                    for (User item : studentListAll) {
//                        if (item.getNama().toLowerCase().contains(in) ||
//                                item.getNim().toLowerCase().contains(in) ||
//                                item.getKelas().toLowerCase().contains(in)) {
//                            filteredList.add(item);
//                        }
//                    }
//                }
//
//                FilterResults filterResults = new FilterResults();
//                filterResults.values = filteredList;
//                return filterResults;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
//                studentList.clear();
//                studentList.addAll((Collection<? extends User>) filterResults.values);
//                notifyDataSetChanged();
//            }
//        };
//    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_nama, tv_kelas, tv_nim;
        ImageView photo;
        LinearLayout ll_list;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.img_list_avatar);
            tv_nama = itemView.findViewById(R.id.tv_list_nama);
            tv_nim = itemView.findViewById(R.id.tv_list_nim);
            tv_kelas = itemView.findViewById(R.id.tv_list_kelas);
            ll_list = itemView.findViewById(R.id.ll_list);

            // Animate
            translateAnim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            ll_list.setAnimation(translateAnim);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_student, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        this.position = position;
        User data = studentList.get(position);

        holder.tv_nama.setText(data.getNama());
        holder.tv_nim.setText(data.getNim());
        holder.tv_kelas.setText(data.getKelas());
        Glide.with(context)
                .load(data.getImageUrl())
                .fitCenter()
                .circleCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.photo);

        // Edit Here
        holder.ll_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("nama",data.getNama());
                intent.putExtra("nim",data.getNim());
                intent.putExtra("kelas",data.getKelas());
                intent.putExtra("img", data.getImageUrl());

                activity.startActivityForResult(intent, 99);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }


}

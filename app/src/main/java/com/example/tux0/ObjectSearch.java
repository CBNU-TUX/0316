package com.example.tux0;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ObjectSearch extends AppCompatActivity {

    public String temp;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adpater;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<recipe> arrayList;
    private ArrayList<recipe> arrayList0;
    private ArrayList<recipe> arrayList1;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);  //layout 재사용

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize((true));
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        arrayList = new ArrayList<>();
        arrayList0 = new ArrayList<>();
        arrayList1 = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("recipe");

        ArrayList<String> Detected = getIntent().getStringArrayListExtra("Detected");

        if(Detected.size() == 1){
            try {
                if(Detected.get(0).equals("cup")){ //임시로 재료명검색을 하기위해 추가
                    temp = "오이";            //식물 사진일 경우 가지로 인식하도록 변경
                }
                //searchActivity의 showrecipe함수를 이용하려 했으나
                // E/RecyclerView: No adapter attached; skipping layout로 실패 직접 선언하기로 바꿈
//                ((SearchActivity) SearchActivity.mContext)
//                        .ShowRecipe(temp); // -> Detected.get(0)
                //ShowRecipe(temp);
            }catch(NullPointerException e){
                Log.d("ObjectSearch","Object Search: Detected.size()==1");
            }
        }
        else if(Detected.size() == 2){
                    //ShowRecipe(Detected.get(0),Detected.get(1));
        }
        else if(Detected.size() == 3){
                    //ShowRecipe(Detected.get(0),Detected.get(1),Detected.get(2));
        }

//        if(){
//            Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
//            ((SearchActivity)SearchActivity.mContext)
//                    .ShowRecipe(DetectedFirst);
//        }
//        else if(!DetectedSecond.equals("empty")){
//            ((SearchActivity)SearchActivity.mContext)
//                    .ShowRecipe(DetectedFirst,DetectedSecond);
//        }
//        else if(!DetectedThird.equals("empty")){
//            ((SearchActivity)SearchActivity.mContext)
//                    .ShowRecipe(DetectedFirst,DetectedSecond,DetectedThird);
//        }
//        else{
//            Toast.makeText(this, "error..아몰랑", Toast.LENGTH_SHORT).show();
//        }
    }
    public void ShowRecipe(String ingre1) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //arrayList초기화
                arrayList.clear();
                //snapshot을 찍어 입력받은 ingre와 일치하는 재료명이 있는지 확인
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(ingre1)) {
                        //재료명 밑의 자식들을 하나하나 snapshot을 찍어 arrayList에 저장
                        for (DataSnapshot snapshot_2 : snapshot.getChildren()) {
                            try{recipe recipe = snapshot_2.getValue(recipe.class);
                            arrayList.add(recipe);}
                            catch(DatabaseException e){}
                        }
                    }
                }
                adpater.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", String.valueOf(error.toException()));
            }
        });
        adpater = new com.example.tux0.recipeAdapter(arrayList, this);
        recyclerView.setAdapter(adpater);
    }

    //1번,2번 재료가 포함된  레시피 arrayList에 저장, 리사이클러뷰로 출력
    public void ShowRecipe(String ingre1, String ingre2) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //arrayList 초기화
                arrayList.clear();
                arrayList0.clear();
                //snapshot을 찍어 입력받은 ingre1와 일치하는 재료명이 있는지 확인

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(ingre1)) {
                        //재료명 밑의 자식들을 하나하나 snapshot을 찍어 arrayList0에 저장
                        for (DataSnapshot snapshot_2 : snapshot.getChildren()) {
                            try{recipe recipe1 = snapshot_2.getValue(recipe.class);
                            arrayList0.add(recipe1);}
                            catch(DatabaseException e){}
                        }
                    }
                }

                //1번 검색창에 입력된 재료가 포함된 레시피들은 arrayList0에 저장된 상태
                //2번 검색창에 입력된 재료가 포함된 레시피 하나하나를 arrayList0의 레시피들과 비교, 일치 할경우(둘다에 포함된경우) arrayList에 저장
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.getKey().equals(ingre2)) {
                        for (DataSnapshot snapshot_2 : snapshot.getChildren()) {
                           try {
                               recipe recipe2 = snapshot_2.getValue(recipe.class);
                               for (int i = 0; i < arrayList0.size(); i++) {
                                   if (arrayList0.get(i).getid().equals(recipe2.getid())) {
                                       arrayList.add(recipe2);
                                   }
                               }
                           }catch(DatabaseException e){}
                        }
                    }
                }
                adpater.notifyDataSetChanged();
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Log.e("MainActivity", String.valueOf(error.toException()));
            }
        });
        adpater = new com.example.tux0.recipeAdapter(arrayList, this);
        recyclerView.setAdapter(adpater);
    }

    public void ShowRecipe(String ingre1, String ingre2, String ingre3) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList0.clear();
                arrayList1.clear();
                arrayList.clear();
                //snapshot을 찍어 입력받은 ingre와 일치하는 재료명이 있는지 확인

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(ingre1)) {
                        //재료명 밑의 자식들을 하나하나 snapshot을 찍어 arrayList에 저장
                        for (DataSnapshot snapshot_2 : snapshot.getChildren()) {
                            try{recipe recipe = snapshot_2.getValue(recipe.class);
                            arrayList0.add(recipe);}
                            catch(DatabaseException e){}
                        }
                    }
                }

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.getKey().equals(ingre2)) {
                        for (DataSnapshot snapshot_2 : snapshot.getChildren()) {
                            try {
                                recipe recipe2 = snapshot_2.getValue(recipe.class);
                                for (int i = 0; i < arrayList0.size(); i++) {
                                    if (arrayList0.get(i).getid().equals(recipe2.getid())) {
                                        arrayList1.add(recipe2);
                                    }
                                }
                            }catch(DatabaseException e)
                            {}                        }
                    }
                }

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.getKey().equals(ingre3)) {
                        for (DataSnapshot snapshot_3 : snapshot.getChildren()) {
                            try {
                                recipe recipe3 = snapshot_3.getValue(recipe.class);
                                for (int i = 0; i < arrayList1.size(); i++) {
                                    if (arrayList1.get(i).getid().equals(recipe3.getid())) {
                                        arrayList.add(recipe3);
                                    }
                                }
                            }catch(DatabaseException e){}
                        }
                    }
                }

                adpater.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", String.valueOf(error.toException()));
            }
        });
        adpater = new com.example.tux0.recipeAdapter(arrayList, this);
        recyclerView.setAdapter(adpater);
    }
}

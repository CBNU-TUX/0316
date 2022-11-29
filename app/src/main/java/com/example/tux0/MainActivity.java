package com.example.tux0;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tux0.databinding.ActivityMainBinding;
import com.example.tux0.image.ObjectDetectionActivity;
import com.example.tux0.ui.opensource_go;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean button_plus_status = false;
    private ActivityMainBinding binding;
    private FloatingActionButton button_plus;
    private FloatingActionButton button_camera;
    private FloatingActionButton button_manual;
    private FloatingActionButton button_search;
    private Button button_add;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseuser;
    private ArrayList<ingre> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    public static RecyclerView recyclerView;
    private RecyclerView.Adapter adpater;
    private RecyclerView.LayoutManager layoutManager;
    private String uid;
    private String email;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    int count;
    //favorite 부분 처럼 처음에 불러오기로 바꾸기

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()){
            case R.id.add_button:
                Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                intent.putExtra("count",count);
                startActivity(intent);
                count=count+1;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view2);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.opensource:
                        Intent Intent = new Intent(getApplicationContext(), opensource_go.class);
                        startActivity(Intent);
                        return true;

                    case R.id.logout:
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        Intent Intent2 = new Intent(getApplicationContext(), loginActivity.class);
                        Intent2.setFlags(Intent2.FLAG_ACTIVITY_NEW_TASK | Intent2.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(Intent2);
                        return true;
                }
                return false;
            }
        });
        //팀소개는 바로 깃허브 연결로

        firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseuser != null) {
            email = firebaseuser.getEmail();
        }


        View header = navigationView.getHeaderView(0);
        TextView user_name = (TextView) header.findViewById(R.id.user_name);
        user_name.setText(email);


        recyclerView = findViewById(R.id.recyclerView_ingre);
        recyclerView.setHasFixedSize((true));
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        arrayList = new ArrayList<>();

        // getInstance()를 사용하여 데이터베이스의 인스턴스를 검색하고 쓰려는 위치를 참조합니다
        database = FirebaseDatabase.getInstance();
        //firebaseAuth 인스턴스 현재 접속된 사용자로 초기화
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) { //접속한 사용자인스턴스가 제대로 불려와졌을 경우

            uid = firebaseUser.getUid(); //uid저장

            //데이터베이스에서 데이터를 읽거나 쓰려면 DatabaseReference 인스턴스가 필요
            //데이터를 읽을 위치로 Users.uid.favorite(사용자가 즐겨찾기한 레시피가 저장되어있음)를 지정
            databaseReference = database.getReference("Users").child(uid).child("ingre");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    arrayList.clear();//arrayList 초기화
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //ingre하위 항목들 하나하나 불러오기
                        try {
                            ingre ingre = snapshot.getValue(ingre.class);
                            arrayList.add(ingre);
                        } catch (DatabaseException e) {

                        }
                        adpater.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", String.valueOf(error.toException()));
                }
            });
        }

        adpater = new com.example.tux0.ingredientAdapter(arrayList, this);
        recyclerView.setAdapter(adpater);

        /*
        Button button_camera = (Button)findViewById(R.id.button_camera);
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(mIntent);
            }
        });
        */
        //카메라 기능 이동

        button_plus = findViewById(R.id.floating_button_plus);
        button_camera = findViewById(R.id.floating_button_camera);
        button_manual = findViewById(R.id.floating_button_manual);
        button_search = findViewById(R.id.floating_button_search);
        button_add = findViewById(R.id.add_button);


        button_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
            }
        });

//        button_camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent mintent = new Intent(MainActivity.this, Camara_cv.class);
//                startActivity(mintent);
//            }
//        });

        button_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, manual.class);
                startActivity(intent);
            }
        });
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this ,SearchActivity.class );
                startActivity(intent);
            }
        });
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

//        button_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(MainActivity.this, PopupActivity.class);
//                intent.putExtra("count",count);
//                startActivity(intent);
//                count=count+1;
//            }
//        });

    }

    public void toggleFab() {
        if (button_plus_status) {
            ObjectAnimator fc_animation = ObjectAnimator.ofFloat(button_camera, "translationY", 0f);
            fc_animation.start();
            ObjectAnimator fe_animation = ObjectAnimator.ofFloat(button_search, "translationY", 0f);
            fe_animation.start();
            ObjectAnimator fg_animation = ObjectAnimator.ofFloat(button_manual, "translationY", 0f);
            fg_animation.start();
            // button_plus.setImageResource(R.drawable.solid_button);
        } else {
            ObjectAnimator fc_animation = ObjectAnimator.ofFloat(button_camera, "translationY", -200f);
            fc_animation.start();
            ObjectAnimator fe_animation = ObjectAnimator.ofFloat(button_search, "translationY", -400f);
            fe_animation.start();
            ObjectAnimator fg_animation = ObjectAnimator.ofFloat(button_manual, "translationY", -600f);
            fg_animation.start();
            // button_plus.setImageResource();
        }
        button_plus_status = !button_plus_status;

    }

    protected void AddIngre(){

        arrayList = new ArrayList<>();
            Intent data = getIntent();
            Log.d("MainActivity","success receivedata ");
                Log.d("MainActivity","success receivedata ");
                //데이터 받기
                String name = data.getStringExtra("name");
                String cnt = data.getStringExtra("cnt");
                String date = data.getStringExtra("date");
                Log.d("MainActivity","success receivedata ");
                // getInstance()를 사용하여 데이터베이스의 인스턴스를 검색하고 쓰려는 위치를 참조합니다
                database = FirebaseDatabase.getInstance();
                //firebaseAuth 인스턴스 현재 접속된 사용자로 초기화
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null){ //접속한 사용자인스턴스가 제대로 불려와졌을 경우

                    uid = firebaseUser.getUid(); //uid저장

                    //데이터베이스에서 데이터를 읽거나 쓰려면 DatabaseReference 인스턴스가 필요
                    //데이터를 읽을 위치로 Users.uid.favorite(사용자가 즐겨찾기한 레시피가 저장되어있음)를 지정
                    databaseReference = database.getReference("Users").child(uid).child("ingre");

                    databaseReference.child(Integer.toString(count)).setValue(name);
                    databaseReference.child(Integer.toString(count)).setValue(cnt);
                    databaseReference.child(Integer.toString(count)).setValue(date);

                }
            }


    public void addIngre(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

 */
}

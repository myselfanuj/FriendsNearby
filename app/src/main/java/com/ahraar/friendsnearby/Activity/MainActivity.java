package com.ahraar.friendsnearby.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ahraar.friendsnearby.Adapter.UsersAdapter;
import com.ahraar.friendsnearby.Model.Users;
import com.ahraar.friendsnearby.Permission;
import com.ahraar.friendsnearby.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {
    private CircleImageView profilePic;
    private TextView mName, mEmail,mEditProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private UsersAdapter usersAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        init();
        //Current user Data
        getCurrentUserData();

        recyclerView = findViewById(R.id.users_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Users");

        FirestoreRecyclerOptions<Users> options = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();

        usersAdapter = new UsersAdapter(options);



        usersAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(usersAdapter);
        usersAdapter.startListening();
    }


    private void init() {

        profilePic = findViewById(R.id.profilePic);
        mName = findViewById(R.id.name_text);
        mEmail = findViewById(R.id.email);
        mEditProfile = findViewById(R.id.editProfile);
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Register.class));
            }
        });
    }



    private void getCurrentUserData() {
        //Get Data from Firestore
        final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = currUser.getUid();
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String contact = documentSnapshot.getString("contact");
                            mEmail.setText(contact);
                            mName.setText(name);

                            String photo = documentSnapshot.getString("photo");
                            Picasso.get().load(photo).placeholder(R.drawable.user_placeholder).into(profilePic);


                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                mAuth.signOut();
                startActivity(new Intent(this, Login.class)
                        .addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        usersAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usersAdapter.stopListening();
    }

}

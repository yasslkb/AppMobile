package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Toolbar maintoolbar;
    private FirebaseAuth mAuth ;
    private FloatingActionButton addPostBtn;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private BottomNavigationView mainBottomNav;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        maintoolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mAuth =FirebaseAuth.getInstance();
        setSupportActionBar(maintoolbar);
        getSupportActionBar().setTitle("blog");

        if(mAuth.getCurrentUser()!= null) {

            addPostBtn = findViewById(R.id.add_post_btn);
            firebaseFirestore = FirebaseFirestore.getInstance();
            mainBottomNav = findViewById(R.id.mainBottomNav);

            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();


            //add a click listener
            addPostBtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);

                }
            });

            replaceFragment(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.bottom_action_home:
                                    replaceFragment(homeFragment);
                                    return true;

                                case R.id.bottom_action_account:
                                    replaceFragment(accountFragment);
                                    return true;
                                case R.id.bottom_action_notif:
                                    replaceFragment(notificationFragment);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentuser == null){
         sendtologin();

        }else{
            current_user_id =mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }
                    }
                    else{

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: "+errorMessage , Toast.LENGTH_LONG).show();

                    }
                }
            });
        }


    }

    private void sendtologin() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem menuItem =menu.findItem(R.id.action_search_btn);
        SearchView searchView = (SearchView) menuItem.getActionView();
        System.out.println("search view "+searchView);
        searchView.setQueryHint("search...");

        menuItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
                                                @Override
                                                public boolean onMenuItemActionExpand(MenuItem item) {
                                                    return true;
                                                }

                                                @Override
                                                public boolean onMenuItemActionCollapse(MenuItem item) {
                                                    homeFragment.loadPost();

                                                    return true;
                                                }
                                            });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
              if (!query.isEmpty()){
                    homeFragment.loadPost(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_logout_btn:
                logout();
                return true;

            case  R.id.action_setting_btn:
                Intent settingsIntent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(settingsIntent);

        }
        return false;
    }

    private void logout() {

        mAuth.signOut();
        sendtologin();

    }

    private void  replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }



}
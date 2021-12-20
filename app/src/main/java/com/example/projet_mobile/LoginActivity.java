package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailtext;
    private EditText passwordtext;
    private Button loginbtn;
    private Button registerbtn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        emailtext = (EditText) findViewById(R.id.login_email);
        passwordtext = (EditText) findViewById(R.id.login_password);
        loginbtn = (Button) findViewById(R.id.login_button);
       registerbtn = (Button) findViewById(R.id.register_button);

       registerbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent registerintent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(registerintent);

           }
       });


       loginbtn.setOnClickListener(
               new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       String email = emailtext.getText().toString();
                       String password = passwordtext.getText().toString();

                       if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull  Task<AuthResult> task) {
                                                    if(task.isSuccessful()){


                                                    }
                                                    else{
                                                        String errorMessage = task.getException().getMessage();
                                                        Toast.makeText(LoginActivity.this, "error" + errorMessage, Toast.LENGTH_SHORT).show();



                                                    }
                            }
                        });
                       }
                   }
               }
       );
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if(currentuser != null){
            sendtomain();
        }
    }

    private void sendtomain() {
        Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}
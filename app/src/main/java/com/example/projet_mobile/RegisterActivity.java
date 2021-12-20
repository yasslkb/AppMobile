package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confirm_pass_field;
    private Button reg_btn;
    private Button reg_login_btn;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_email_field =(EditText) findViewById(R.id.reg_email);
        reg_pass_field =(EditText) findViewById(R.id.reg_pass);
        reg_confirm_pass_field =(EditText) findViewById(R.id.reg_confirm_pass);
        reg_btn = (Button) findViewById(R.id.reg_btn);
        reg_login_btn = (Button) findViewById(R.id.reg_login_btn);
        mAuth = FirebaseAuth.getInstance();

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
            reg_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = reg_email_field.getText().toString();
                    String password = reg_pass_field.getText().toString();
                    String confirm_password = reg_confirm_pass_field.getText().toString();

                    if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                            && !TextUtils.isEmpty(confirm_password)){
                        
                        if(password.equals(confirm_password)){
                            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener
                                    (new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull  Task<AuthResult> task) {
if (task.isSuccessful()){
Intent setupintent = new Intent(RegisterActivity.this, SetupActivity.class);
startActivity(setupintent);
finish();
}
else{

    String error = task.getException().getMessage();
    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
}
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,
                                    "password and confirm password don't match",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });



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

        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}
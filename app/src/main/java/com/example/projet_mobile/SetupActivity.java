package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {
    private CircleImageView setupimage;
    private Uri mainimage =null;
    private EditText setupname;
    private Button setupbtn;
    private StorageReference storageref;
    private String user_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
private boolean ischanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setuptoolbar = (Toolbar) findViewById(R.id.setupToolbar);
        setSupportActionBar(setuptoolbar);
        getSupportActionBar().setTitle("account settings");
        firestore = FirebaseFirestore.getInstance();
        setupbtn = findViewById(R.id.setup_btn);
        setupname = findViewById(R.id.setup_name);
        mAuth = FirebaseAuth.getInstance();
        storageref = FirebaseStorage.getInstance().getReference();
user_id = mAuth.getCurrentUser().getUid();
setupbtn.setEnabled(false);
firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
    @Override
    public void onComplete(@NonNull  Task<DocumentSnapshot> task) {
if(task.isSuccessful()){
    if(task.getResult().exists()){
        String name = task.getResult().getString("name");
        String image = task.getResult().getString("image");
        mainimage = Uri.parse(image);

        setupname.setText(name);
        RequestOptions options = new RequestOptions();
     // ajouter drawable sous xml   options.placeholder(R.drawable.default_image);
        Glide.with(SetupActivity.this).load(image).into(setupimage);


    }else{

    }
}else{

}

    }
});
        setupimage = findViewById(R.id.setup_image);
        setupimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        imagepicker();
                    }
                }
                else{
                    imagepicker();
                }

            }
        });

        setupbtn.setEnabled(true);
        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ischanged) {
                    String user_name = setupname.getText().toString();

                    if (!TextUtils.isEmpty(user_name) && mainimage != null) {

                        user_id = mAuth.getCurrentUser().getUid();
                        StorageReference image_path = storageref.child("profile_images")
                                .child(user_id + ".jpg");

                        image_path.putFile(mainimage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Uri download_uri = image_path.getDownloadUrl().getResult();
                                                                                        Map<String, String> usermap = new HashMap<>();
                                                                                        usermap.put("name", user_name);
                                                                                        usermap.put("image", download_uri.toString());
                                                                                        firestore.collection("Users").document(user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(SetupActivity.this, "succes", Toast.LENGTH_SHORT).show();
                                                                                                    Intent mainintent = new Intent(SetupActivity.this, MainActivity.class);
                                                                                                    startActivity(mainintent);
                                                                                                    finish();
                                                                                                } else {
                                                                                                    //ajouter le toast aprés
                                                                                                }

                                                                                            }
                                                                                        });
                                                                                    } else {
                                                                                        Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                    }


                                                                                }
                                                                            }
                        );


                    }

                }
                else{


                }
            }
        });}

    private void imagepicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data){
                    super.onActivityResult(requestCode, resultCode, data);
                    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == RESULT_OK) {
                            mainimage = result.getUri();
                            setupimage.setImageURI(mainimage);
                            ischanged =true;
                        }
                        else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Exception error = result.getError();
                        }
                    }
                }

}
package com.example.projet_mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private Uri postImageUri=null;


    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;


    //onCreate Methode
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc= findViewById(R.id.new_post_desc);
        newPostBtn= findViewById(R.id.post_btn);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();


        //on click listener pour le button newpostimage pours setter une image pour le post
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on crop image on fixant  un width min et un height min de 512
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });


        //click listener pour le button d'ajout d'un post
        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //la description du post
                String desc = newPostDesc.getText().toString();

                //tester si la description est non vide  et l'image du post et non null
                if (!TextUtils.isEmpty(desc) && postImageUri != null) {

                    //un nom de l'image aleatoire pour le stocker sur firebase.
                    String randomeName = UUID.randomUUID().toString();
                    //obtien un reference du document sur firebase pour l'utiliser apres pour le stockage sur firebase.
                    StorageReference filePath = storageReference.child("post_images").child(randomeName + ".jpg");

                    //stocker le post sur firebase.
                    filePath.putFile(postImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    File newImageFile = new File(postImageUri.getPath());
                                    // compresser l'image pour stocker un thumbnail de la meme image sur firebase , et l'utiliser au lieu de l'image originale.
                                    try{
                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                    byte[] thumbData = baos.toByteArray();

                                    //une upload task est lance ici.
                                    UploadTask uploadTask =storageReference.child("post_images/thumbs").child(randomeName + ".jpg").putBytes(thumbData);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()  {

                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){

                                            final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();

                                            //le stockage sur firebase on sittons tous les champs d'un post
                                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String downloadthumbUri = taskSnapshot.getStorage().getDownloadUrl().toString();
                                                    final String downloadUri = uri.toString();
                                                    Map<String, Object> postMap = new HashMap<>();
                                                    postMap.put("image_url", downloadUri);
                                                    postMap.put("image_thumb",downloadthumbUri);
                                                    postMap.put("desc", desc);
                                                    postMap.put("user_id", current_user_id);
                                                    postMap.put("timestamp", FieldValue.serverTimestamp());

                                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                                Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);
                                                                finish();

                                                            } else {

                                                            }
                                                        }
                                                    });


                                                }
                                            });

                                        }

                                    }).addOnFailureListener( new OnFailureListener() {

                                        public void onFailure(@NonNull Exception e){

                                            //we need to handle errors here!

                                        }



                                    });




                                }
                            });

                }
            }
        });




    }



    //on lance l'activity de croper l'image et on obtien le resultat.
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data){
        super.onActivityResult(requestCode , resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();

            }

        }

    }


}
package com.devanant.bee.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.devanant.bee.Database.TinyDB;
import com.devanant.bee.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class EditProfile extends AppCompatActivity {

    private TextView TextName, TextBio, ImageInsta,ImageFB, TextInterest;
    private String Username,Facebook, Instagram,Bio;
    private ImageView profilePic;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private ArrayList<String> Interests;
    private TinyDB tinyDB;
    private String UserID;
    private Map<String, Object> map;
    private FloatingActionButton doneBtn;
    private Bitmap compressedImageFile;
    private byte[] finalImage;
    private Integer IMAGE_ADDED=0;
    private String ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tinyDB=new TinyDB(getApplicationContext());
        Interests=new ArrayList<>();
        firestore= FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        map=new HashMap<>();
        map=tinyDB.getObject("UserProfile",map.getClass());

        doneBtn=findViewById(R.id.doneBtn);
        TextName=findViewById(R.id.edit_name);
        TextBio=findViewById(R.id.edit_bio);
        TextInterest=findViewById(R.id.edit_interest);
        ImageInsta=findViewById(R.id.edit_instagram);
        ImageFB=findViewById(R.id.edit_facebook);
        profilePic=findViewById(R.id.profileImage2);

        //getting current userID
        UserID=mAuth.getCurrentUser().getUid();

        //getting image from firestore
        String ImageEncoded=tinyDB.getString("ProfilePic");
        if(!ImageEncoded.isEmpty()){
            byte[] decodedByte = Base64.decode(ImageEncoded, 0);
            Bitmap image= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            profilePic.setImageBitmap(image);
        }

        TextName.setText(map.get("Username").toString());
        Interests= (ArrayList<String>) map.get("Interest");
        ImageUri=map.get("ProfilePic").toString();

        if(map.get("Bio").toString().isEmpty()){
            TextBio.setText("Edit your profile to add bio");
        }
        TextBio.setText(map.get("Bio").toString());

        if(!map.get("Instagram").toString().isEmpty()){
            ImageInsta.setText(map.get("Instagram").toString());
        }

        if(!map.get("Facebook").toString().isEmpty()){
            ImageFB.setText(map.get("Facebook").toString());
        }

        setupText();

        doneBtn.setOnClickListener(v->{
            Username=TextName.getText().toString();
            Facebook=ImageFB.getText().toString();
            Instagram=ImageInsta.getText().toString();
            Bio=TextBio.getText().toString();

            if(check()){
                EditUserProfile();
            }
        });

        TextInterest.setOnClickListener(v->{
            Intent i=new Intent(EditProfile.this, ChooseInterest.class);
            startActivity(i);
        });

        profilePic.setOnClickListener(v->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(9,16).start(EditProfile.this);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            else {
                //permission is automatically granted on sdk<23 upon installation
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(9,16).start(EditProfile.this);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(9,16).start(EditProfile.this);
            //resume tasks needing this permission
        }
    }

    private boolean check() {
        if(Username.isEmpty()){
            TextName.setError("Username is empty");
            return false;
        }else{
            if(!Instagram.isEmpty()){
                if(!URLUtil.isValidUrl(Instagram)){
                    ImageInsta.setError("Enter valid URL or else leave empty");
                    return false;
                }else{
                    if(!Instagram.contains("instagram") || !Instagram.toLowerCase().contains("instagram")){
                        ImageInsta.setError("Enter valid URL or else leave empty");
                        return false;
                    }
                }
            }
            if(!Facebook.isEmpty()){
                if(!URLUtil.isValidUrl(Facebook)){
                    ImageFB.setError("Enter valid URL or else leave empty");
                    return false;
                }else{
                    if(!Facebook.contains("facebook") || !Facebook.toLowerCase().contains("facebook")){
                        ImageFB.setError("Enter valid URL or else leave empty");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void setupText() {

        String uInterest="";
        for(int i=0;i<Interests.size();i++){
            uInterest=uInterest+Interests.get(i)+" | ";
        }
        TextInterest.setText(uInterest);
    }

    private void EditUserProfile() {
        Map<String, Object> nMap=new HashMap<>();
        nMap.put("Username", Username);
        nMap.put("Instagram", Instagram);
        nMap.put("Facebook", Facebook);
        nMap.put("Bio",Bio);
        nMap.put("Organisation", map.get("Organisation"));
        nMap.put("Interest",Interests);
        nMap.put("PhoneNumber",map.get("PhoneNumber"));
        nMap.put("ProfilePic",ImageUri);

        Log.i("ProfileEdit", "EditUserProfile: "+nMap);

        tinyDB.putObject("UserProfile", nMap);
        firestore.collection("Users").document(mAuth.getCurrentUser().getUid()).update(nMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EditProfile.this, "Edited Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("ProfileEdit", "onFailure: "+e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            Uri resultUri=result.getUri();
            Picasso.get().load(resultUri).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    profilePic.setImageBitmap(bitmap);
                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10,byteArrayOutputStream );
                    finalImage=byteArrayOutputStream.toByteArray();
                    String imageEncoded = Base64.encodeToString(finalImage, Base64.DEFAULT);
                    tinyDB.putString("ProfilePic", imageEncoded);
                    uploadToFirebaseStorage();
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
//            Log.i("DebugPicker", "onActivityResult: "+compressedImageFile.getByteCount());
//            File actualImage=new File(resultUri.getPath());
//            Log.i("DebugPicker", "onActivityResult: "+actualImage.getAbsolutePath());
//            try {
//                compressedImageFile = new Compressor(this)
//                        .setMaxWidth(720)
//                        .setMaxHeight(1280)
//                        .setQuality(30)
//                        .compressToBitmap(actualImage);
//                Log.i("DebugPicker", "onActivityResult: "+compressedImageFile.getByteCount());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
//            compressedImageFile.compress(Bitmap.CompressFormat.JPEG,80,byteArrayOutputStream);
//            finalImage=byteArrayOutputStream.toByteArray();
//            String imageEncoded = Base64.encodeToString(finalImage, Base64.DEFAULT);
//            tinyDB.putString("ProfilePic", imageEncoded);
//            Picasso.get().load(resultUri).into(profilePic);
//            uploadToFirebaseStorage();
        }
    }


    private void uploadToFirebaseStorage() {
        final StorageReference Fileref = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/" + "profile.jpg");
        Fileref.putBytes(finalImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUri=uri.toString();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Interests=tinyDB.getListString("UserInterest");
        setupText();
    }
}
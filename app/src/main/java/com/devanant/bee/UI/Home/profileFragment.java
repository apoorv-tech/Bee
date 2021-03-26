package com.devanant.bee.UI.Home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devanant.bee.Database.TinyDB;
import com.devanant.bee.R;
import com.devanant.bee.UI.EditProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
//import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


public class profileFragment extends Fragment {

    private static final String TAG ="Profile_LOG" ;
    //private String Name,Org,Bio,Interest,Instagram,facebook;
    private TextView TextName, TextOrg, TextBio, TextSkill1, TextSkill2, TextSkill3;
    private ImageView ImageInsta,ImageFB;
    private ImageView profilePic;
    private ArrayList<String> Interests;
    private TinyDB tinyDB;
    private Map<String, Object> map;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String UserID;
    private StorageReference storageReference;
    private View view;
    private FloatingActionButton BtnEditProf;
    private Uri imageUri;
    private Integer PROFILE_PIC=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB=new TinyDB(getContext());
        Interests=new ArrayList<>();
        firestore= FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        map=new HashMap<>();
        map=tinyDB.getObject("UserProfile",map.getClass());
        if(map.isEmpty()){
            firestore.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    map=snapshot.getData();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: Failed to load");
                }
            });
        }

    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    private void loadProfileImage() {
        String ImageEncoded=tinyDB.getString("ProfilePic");
        if(ImageEncoded.isEmpty()) {
            Log.i(TAG, "loadProfileImage: Empty");
            StorageReference profileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profilePic);
                    Picasso.get().load(uri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.i(TAG, "onBitmapLoaded: Saved to tinyDB");
                            tinyDB.putString("ProfilePic",encodeTobase64(bitmap));
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.i(TAG, "onBitmapFailed: "+e.getMessage());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                }
            });
        }else{
            Log.i(TAG, "loadProfileImage: Found");
            byte[] decodedByte = Base64.decode(ImageEncoded, 0);
            Bitmap image=BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            profilePic.setImageBitmap(image);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_profile, container, false);

        TextName=view.findViewById(R.id.profileName);
        TextOrg=view.findViewById(R.id.profileOrg);
        TextBio=view.findViewById(R.id.profileBio);
        TextSkill1=view.findViewById(R.id.profileSkill1);
        TextSkill2=view.findViewById(R.id.profileSkill2);
        TextSkill3=view.findViewById(R.id.profileSkill3);
        ImageInsta=view.findViewById(R.id.profInsta);
        ImageFB=view.findViewById(R.id.profFacebook);
        profilePic=view.findViewById(R.id.profileImage);
        BtnEditProf=view.findViewById(R.id.BtnEditProf);

        //getting current userID
        UserID=mAuth.getCurrentUser().getUid();

        //getting image from firestore
        //loadProfileImage();




        //Edit Profile
        BtnEditProf.setOnClickListener(v->{
            Intent i=new Intent(getActivity(), EditProfile.class);
            startActivity(i);
        });

        //setUpProfile();


        return view;
    }

    private void setUpProfile() {

        TextName.setText(map.get("Username").toString());
        TextOrg.setText(map.get("Organisation").toString());
        Interests= (ArrayList<String>) map.get("Interest");
        TextSkill1.setVisibility(View.INVISIBLE);
        TextSkill2.setVisibility(View.INVISIBLE);
        TextSkill3.setVisibility(View.INVISIBLE);
        for(int i=0;i<Interests.size();i++){
            if(i==0){
                TextSkill1.setText(Interests.get(i));
                TextSkill1.setVisibility(View.VISIBLE);
            }else if(i==1){
                TextSkill2.setText(Interests.get(i));
                TextSkill2.setVisibility(View.VISIBLE);
            }else if(i==2){
                TextSkill3.setText(Interests.get(i));
                TextSkill3.setVisibility(View.VISIBLE);
                break;
            }
        }
        if(map.get("Bio").toString().isEmpty()){
            TextBio.setText("Edit your profile to add bio");
        }
        TextBio.setText(map.get("Bio").toString());

        if(!map.get("Instagram").toString().isEmpty()){
            ImageInsta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(map.get("Instagram").toString()));
                    startActivity(i);
                }
            });
        }else{
            ImageInsta.setAlpha(0.3f);
        }

        if(!map.get("Facebook").toString().isEmpty()){
            ImageFB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(map.get("Facebook").toString()));
                    startActivity(i);
                }
            });
        }else{
            ImageFB.setAlpha(0.3f);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        map=tinyDB.getObject("UserProfile",map.getClass());
        loadProfileImage();
        setUpProfile();

    }
}
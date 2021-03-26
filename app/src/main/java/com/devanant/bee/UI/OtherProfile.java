package com.devanant.bee.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devanant.bee.R;
import com.devanant.bee.UI.Home.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OtherProfile extends AppCompatActivity {

    private ImageView profilePic;
    private TextView TextName, TextBio, TextSkill1, TextSkill2, TextSkill3;
    private UserModel userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        TextName=findViewById(R.id.oUsername);
        TextBio=findViewById(R.id.oBioText);
        //profilePic=findViewById(R.id.oProImage);
        TextSkill1=findViewById(R.id.oprofileSkill1);
        TextSkill2=findViewById(R.id.oprofileSkill2);
        TextSkill3=findViewById(R.id.oprofileSkill3);

        userModel= (UserModel) getIntent().getSerializableExtra("ProfileModel");
        TextName.setText(userModel.getUsername());
        TextBio.setText(userModel.getBio());
        ArrayList<String> Interests = userModel.getInterest();
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
        String url=userModel.getProfilePic();
        //Picasso.get().load(url).into(profilePic);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
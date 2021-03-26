package com.devanant.bee.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devanant.bee.R;
import com.devanant.bee.UI.Home.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class InterestBrowse extends AppCompatActivity implements BrowseInterestAdapter.SelectedPager{

    private static final String TAG = "InterestBrowseDebug";
    private String Interest;
    private List<UserModel> userModels;
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastSnapshot;
    private BrowseInterestAdapter adapter;
    private ViewPager2 BrowseViewPager;
    private static Integer LAST_VISIBLE=1;
    private static Integer IS_LOADING=0;
    private TextView TextName, TextBio, TextSkill1, TextSkill2, TextSkill3;
    private UserModel userModel;
    private ImageView blurBg;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_browse);

        TextName=findViewById(R.id.bUsername);
        TextBio=findViewById(R.id.bBioText);
        TextSkill1=findViewById(R.id.bprofileSkill1);
        TextSkill2=findViewById(R.id.bprofileSkill2);
        TextSkill3=findViewById(R.id.bprofileSkill3);
        loading=findViewById(R.id.browseLoading);
        blurBg=findViewById(R.id.blurBackground);

        firestore=FirebaseFirestore.getInstance();
        Interest=getIntent().getStringExtra("Interest");
        BrowseViewPager=findViewById(R.id.BrowseViewPager);

        userModels=new ArrayList<>();
        adapter=new BrowseInterestAdapter(userModels, this);
        BrowseViewPager.setAdapter(adapter);

        Toast.makeText(getApplicationContext(), Interest,Toast.LENGTH_SHORT).show();

        setFirstBatch();

        BrowseViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                userModel=userModels.get(position);
                setupView();
            }
        });

        Log.i(TAG, "onCreate: "+userModels.toString());


    }

    private void setupView() {
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
    }

    private void setFirstBatch() {
        firestore.collection("Users").whereArrayContains("Interest", Interest).limit(2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.i(TAG, "onSuccess: "+queryDocumentSnapshots.size());
                for(DocumentSnapshot snapshot:queryDocumentSnapshots){
                    userModels.add(snapshot.toObject(UserModel.class));
                    Log.i(TAG, "onSuccess: "+userModels.size());
                    adapter.notifyDataSetChanged();
                    lastSnapshot=snapshot;
                }
                if(queryDocumentSnapshots.size()<2){
                    LAST_VISIBLE=0;
                }
                blurBg.setVisibility(View.INVISIBLE);;
                loading.setVisibility(View.INVISIBLE);
                IS_LOADING=0;
                BrowseViewPager.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged()
                    {
                        View view = (View)BrowseViewPager.getChildAt(BrowseViewPager.getChildCount() - 1);

                        int diff = (view.getBottom() - (BrowseViewPager.getHeight() + BrowseViewPager
                                .getScrollY()));

                        if (diff == 0 && LAST_VISIBLE==1 && IS_LOADING==0) {
                            loadLatestPost();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: "+e.getMessage());
            }
        });
    }

    private void loadLatestPost() {
        IS_LOADING=1;
        Query nextquery=firestore.collection("Users")
                .whereArrayContains("Interest", Interest).startAfter(lastSnapshot).limit(2);
        nextquery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    Log.i("LatestPost", "onSuccess: Empty");
                }else if(userModels.size()%2==0) {
                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc: snapshots){
                        userModels.add(doc.toObject(UserModel.class));
                    }
                    adapter.notifyDataSetChanged();
                    lastSnapshot=snapshots.get(snapshots.size()-1);
                    IS_LOADING=0;
                    if(snapshots.size()<2){
                        Log.i("LatestPost", "onScrollChanged: limit reached");
                        LAST_VISIBLE=0;
                    }
                    BrowseViewPager.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged()
                        {
                            View view = (View)BrowseViewPager.getChildAt(BrowseViewPager.getChildCount() - 1);

                            int diff = (view.getBottom() - (BrowseViewPager.getHeight() + BrowseViewPager
                                    .getScrollY()));

                            if (diff == 0 && LAST_VISIBLE==1 && IS_LOADING==0) {
                                Log.i("LatestPost", "onScrollChanged: More");
                                loadLatestPost();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void selectedpager(UserModel viewPagerModel) {
        Intent i=new Intent(InterestBrowse.this, OtherProfile.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
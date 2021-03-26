package com.devanant.bee.UI.Home;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devanant.bee.Database.TinyDB;
import com.devanant.bee.R;
import com.devanant.bee.UI.CreateProfile;
import com.devanant.bee.UI.EmailLogin;
import com.devanant.bee.UI.GridSpacingItemDecoration;
import com.devanant.bee.UI.InterestBrowse;
import com.devanant.bee.UI.LoginHome;
import com.devanant.bee.UI.OtherProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class homeFragment extends Fragment implements UserAdapter.SelectedPager, CategoryAdapter.SelectedViewPager{

    private static final String TAG = "HomeFragment";
    private FirebaseFirestore fstore;
    private List<UserModel> userModels;
    private UserAdapter userAdapter;
    private RecyclerView suggestionPager;
    private ArrayList<String> interest;
    private FirebaseAuth mAuth;
    private RecyclerView categoryRecyclerView;
    private List<String> titles;
    private List<Integer> mImages;
    private TinyDB tinyDB;
    private CategoryAdapter categoryAdapter;
    private CircleImageView circleImageView;
    private StorageReference storageReference;
    private TextView userSearch;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fstore=FirebaseFirestore.getInstance();
        tinyDB=new TinyDB(getContext());
        userModels=new ArrayList<>();
        interest=new ArrayList<>();
        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

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
                    Picasso.get().load(uri).into(circleImageView);
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
            Bitmap image= BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            circleImageView.setImageBitmap(image);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        interest=tinyDB.getListString("UserInterest");
        fstore.collection("Users").whereArrayContainsAny("Interest",interest).limit(5).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty())
                {
                    for(QueryDocumentSnapshot doc:value) {
                        if(!doc.getId().equals(mAuth.getCurrentUser().getUid())){
                        UserModel model = doc.toObject(UserModel.class);
                        userModels.add(model);
                        userAdapter.notifyDataSetChanged();
                        Log.i("HomeFragmentSuggestion", model.getUsername());
                        }
                    }
                    for(QueryDocumentSnapshot doc:value) {
                        if(!doc.getId().equals(mAuth.getCurrentUser().getUid())){
                            UserModel model = doc.toObject(UserModel.class);
                            userModels.add(model);
                            userAdapter.notifyDataSetChanged();
                            Log.i("HomeFragmentSuggestion", model.getUsername());
                        }
                    }
                    for(QueryDocumentSnapshot doc:value) {
                        if(!doc.getId().equals(mAuth.getCurrentUser().getUid())){
                            UserModel model = doc.toObject(UserModel.class);
                            userModels.add(model);
                            userAdapter.notifyDataSetChanged();
                            Log.i("HomeFragmentSuggestion", model.getUsername());
                        }
                    }
                }
                else{
                    Log.i("HomeFragmentSuggestion","Empty"+interest.toString());
                }

            }
        });

        View root=inflater.inflate(R.layout.fragment_home, container, false);
        suggestionPager=root.findViewById(R.id.SuggestionViewPager);
        categoryRecyclerView=root.findViewById(R.id.CategoryRecyclerView);
        userSearch=root.findViewById(R.id.userSearch);
        userSearch.setOnClickListener(v->{
            Intent i=new Intent(getActivity(), SearchUsersTags.class);
            ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    new Pair<>(userSearch, "SearchBar"));
            startActivity(i, options.toBundle());
        });

        //circleImageView=root.findViewById(R.id.circleImageView);
        //loadProfileImage();



        titles=new ArrayList<>();
        mImages=new ArrayList<>();
        categoryAdapter=new CategoryAdapter(getContext(),titles,mImages,this);
        mImages.add(R.drawable.dance);
        mImages.add(R.drawable.music);
        mImages.add(R.drawable.photography);
        mImages.add(R.drawable.design);
        mImages.add(R.drawable.tvshows);
        mImages.add(R.drawable.dramatics);
        mImages.add(R.drawable.reading);
        mImages.add(R.drawable.dance);
        mImages.add(R.drawable.music);
        mImages.add(R.drawable.photography);
        mImages.add(R.drawable.tvshows);
        mImages.add(R.drawable.dramatics);
        mImages.add(R.drawable.reading);
        mImages.add(R.drawable.dance);



        titles.add("Dance");
        titles.add("Music");
        titles.add("Photography");
        titles.add("Designing");
        titles.add("TV Shows");
        titles.add("Dramatics");
        titles.add("Reading");
        titles.add("Dance");
        titles.add("Music");
        titles.add("Photography");
        titles.add("Designing");
        titles.add("Videography");
        titles.add("Dramatics");
        titles.add("Reading");

        //GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int spanCount = 3; // 3 columns
        int spacing = 50; // 50px
        boolean includeEdge = true;
       // categoryRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setAdapter(categoryAdapter);
        //categoryRecyclerView.setNestedScrollingEnabled(false);
        suggestionPager.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        suggestionPager.setHasFixedSize(true);
        setUpViewPager();
        return root;
    }

    private void setUpViewPager() {
        userAdapter=new UserAdapter(userModels,this);
        suggestionPager.setAdapter(userAdapter);
    }


    @Override
    public void selectedpager(UserModel viewPagerModel) {
        Intent i=new Intent(getActivity(), OtherProfile.class);
        i.putExtra("ProfileModel", viewPagerModel);
        startActivity(i);
    }

    @Override
    public void selectedViewpager(String title) {
        Intent i=new Intent(getActivity(), InterestBrowse.class);
        i.putExtra("Interest", title);
        startActivity(i);
    }
}
package com.devanant.bee.UI.Home;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import com.devanant.bee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersTags extends AppCompatActivity implements AdapterUserTagSearch.SelectedItem {
    private EditText searchbar;
    private ImageButton searchbutton;
    private RecyclerView searchresults;
    private FirebaseFirestore fstore;
    private List<UserModel> userModel;
    private AdapterUserTagSearch userAdapter;
    private FirebaseAuth mAuth;
    private String UserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users_tags);
        searchbar = (EditText) findViewById(R.id.searchBar);
//        searchbutton = (ImageButton)findViewById(R.id.searchButton);
        searchresults = (RecyclerView)findViewById(R.id.searchResults);
        fstore=FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        userModel=new ArrayList<>();


        userAdapter=new AdapterUserTagSearch(userModel,this);
        searchresults.setAdapter(userAdapter);

        searchresults.hasFixedSize();
        searchresults.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchKeyword = searchbar.getText().toString();
                if (searchKeyword.equals("")){
                    fstore.collection("Users")
                            .limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            userModel.clear();
                            for (QueryDocumentSnapshot doc : value) {

                                if(doc.getId().equals(mAuth.getCurrentUser().getUid())){
                                    Log.i("SameUser","Sameuser");
                                }

                                else{
                                    Log.i("searchCheck", "onEvent:" + value.size());

                                    UserModel set = doc.toObject(UserModel.class);
                                    userModel.add(set);
                                    Log.i("searchCheck", "onEvent: "+set.getUsername());
                                    userAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
                else{
                    fstore.collection("Users").whereEqualTo("Username",searchKeyword)
                            .limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            userModel.clear();
                            for (QueryDocumentSnapshot doc : value) {

                                if(doc.getId().equals(mAuth.getCurrentUser().getUid())){
                                    Log.i("SameUser","Sameuser");
                                }

                                else{
                                    Log.i("searchCheck", "onEvent:" + value.size());

                                    UserModel set = doc.toObject(UserModel.class);
                                    userModel.add(set);
                                    Log.i("searchCheck", "onEvent: "+set.getUsername());
                                    userAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKeyword = editable.toString();
                Log.d("SearchUser", "afterTextChanged: "+editable.toString());

            }
        });
    }

    @Override
    public void selectedItem(UserModel userModel) {
        Log.i("sentIntent", "selectedItem: " + userModel.getUsername());
    }
}
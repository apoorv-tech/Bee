package com.devanant.bee.UI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devanant.bee.R;
import com.devanant.bee.UI.Home.UserModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BrowseInterestAdapter extends RecyclerView.Adapter<BrowseInterestAdapter.mViewHolder>{

    private List<UserModel> viewPagerModel;
    private SelectedPager selectedpager;

    public BrowseInterestAdapter(List<UserModel> viewPagerModel,SelectedPager selectedpager){
        this.viewPagerModel=viewPagerModel;
        this.selectedpager=selectedpager;
    }

    @NonNull
    @Override
    public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_browse, parent, false);
        //view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {
        holder.setProfilePic(viewPagerModel.get(position).getProfilePic());
    }

    @Override
    public int getItemCount() {
        return viewPagerModel.size();
    }


    public interface SelectedPager{
        void selectedpager(UserModel viewPagerModel);
    }

    public class mViewHolder extends RecyclerView.ViewHolder{
        View view;
        ImageView ProfilePic;

        public mViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedpager.selectedpager(viewPagerModel.get(getAdapterPosition()));
                }
            });
        }
        public void setProfilePic(String url){
            ProfilePic=view.findViewById(R.id.bProImage);
            Picasso.get().load(url).into(ProfilePic);
            Log.i("ProfileURL",url);
        }

    }
}

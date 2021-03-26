package com.devanant.bee.UI.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devanant.bee.R;
import com.devanant.bee.UI.BrowseInterestAdapter;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private Context context;
    private List<String> titles;
    private  List<Integer> images;
    private SelectedViewPager selectedPager;

    public CategoryAdapter(Context context,List<String> titles,List<Integer> images, SelectedViewPager selectedPager)
    {
        this.context=context;
        this.titles=titles;
        this.images=images;
        this.selectedPager=selectedPager;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.category_grid_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
     holder.mTextView.setText(titles.get(position));
     holder.mImageView.setImageResource(images.get(position));
    }

    public interface SelectedViewPager{
        void selectedViewpager(String title);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView mImageView;
        TextView mTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=itemView.findViewById(R.id.categoryImage);
            mTextView=itemView.findViewById(R.id.categoryTitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPager.selectedViewpager(titles.get(getAdapterPosition()));
                }
            });
        }
    }
}

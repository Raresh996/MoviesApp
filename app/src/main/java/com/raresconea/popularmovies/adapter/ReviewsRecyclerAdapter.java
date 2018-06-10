package com.raresconea.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.raresconea.popularmovies.R;
import com.raresconea.popularmovies.model.Review;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rares on 3/16/2018.
 */

public class ReviewsRecyclerAdapter extends RecyclerView.Adapter<ReviewsRecyclerAdapter.ReviewHolder>{

    private Context context;
    private List<Review> reviews;

    public ReviewsRecyclerAdapter(Context context) {
        this.context = context;
        reviews = new ArrayList<>();
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_recycleradapter_row, parent, false);
        return new ReviewsRecyclerAdapter.ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.authorName.setText(context.getString(R.string.written_by) + " " + review.getAuthor());
        holder.reviewContent.setText(review.getContent());

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    class ReviewHolder extends RecyclerView.ViewHolder {

        TextView authorName;
        TextView reviewContent;


        public ReviewHolder(View itemView) {
            super(itemView);

            authorName = itemView.findViewById(R.id.authorName_tv);
            reviewContent = itemView.findViewById(R.id.reviewContent_tv);
        }
    }

}

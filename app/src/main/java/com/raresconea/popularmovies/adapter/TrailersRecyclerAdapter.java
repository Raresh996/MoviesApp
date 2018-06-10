package com.raresconea.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.raresconea.popularmovies.R;
import com.raresconea.popularmovies.model.Trailer;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Rares on 3/14/2018.
 */

public class TrailersRecyclerAdapter extends RecyclerView.Adapter<TrailersRecyclerAdapter.TrailerHolder> {

    private Context context;

    private List<Trailer> trailers;

    public TrailersRecyclerAdapter(Context context) {
        this.context = context;
        trailers = new ArrayList<>();
    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailers_recycleradapter_row, parent, false);
        return new TrailerHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        final Trailer trailer = trailers.get(position);

        holder.trailerName.setText(trailer.getName());

        String key = trailer.getKey();
        final String youtubeUrl = "https://www.youtube.com/watch?v=" + key;

        /*
            I use this link to get a thumbnail image of my video trailer
         */
        String urlToGetVideoThumbnailImage = "https://i1.ytimg.com/vi/" + key + "/0.jpg";

        Glide
                .with(context)
                .load(urlToGetVideoThumbnailImage)
                .into(holder.trailerImage);

            /*
                If there is only one trailer it is not necessary to
                change the width of the layout so two trailers can be
                seen
             */
        if (trailers.size() > 1) {
            /*
            Set the width of the relative layout to be smaller that the screen
            size so the user can see a trailer full width and also the beginning
            of the next trailer. In this way the user will know if there are more
            trailers for this movie
         */
            holder.relativeLayout.getLayoutParams().width = dpToPx(getScreenWidthInDPs(context) - 100);
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = trailer.getKey();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("VIDEO_ID", key);

                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public void setTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    /*
        Get the width of the screen in dp
     */
    private int getScreenWidthInDPs(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int widthInDP = Math.round(dm.widthPixels / dm.density);
        return widthInDP;
    }

    /*
        Convert from dp to px
     */
    private int dpToPx(int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    class TrailerHolder extends RecyclerView.ViewHolder {

        ImageView trailerImage;
        ImageView playButton;
        TextView trailerName;
        RelativeLayout relativeLayout;

        public TrailerHolder(View itemView) {
            super(itemView);

            trailerImage = itemView.findViewById(R.id.trailer_iv);
            playButton = itemView.findViewById(R.id.playButton_iv);
            trailerName = itemView.findViewById(R.id.trailerName_tv);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_id);


        }
    }

}

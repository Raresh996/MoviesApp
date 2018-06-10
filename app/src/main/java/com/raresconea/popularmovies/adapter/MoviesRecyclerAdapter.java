package com.raresconea.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.raresconea.popularmovies.MovieDetailActivity;
import com.raresconea.popularmovies.R;
import com.raresconea.popularmovies.model.Movie;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rares on 3/10/2018.
 */

public class MoviesRecyclerAdapter extends RecyclerView.Adapter<MoviesRecyclerAdapter.MovieHolder> {

    private Context context;
    private List<Movie> movies;

    public MoviesRecyclerAdapter(Context context) {
        this.context = context;
        movies = new ArrayList<>();
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movies_recycleradapter_row, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, final int position) {
        final Movie movie = movies.get(position);

        String posterPath = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();

        Glide
                .with(context)
                .load(posterPath)
                .into(holder.posterImage);


        holder.movieTitleText.setText(movie.getOriginalTitle());
        holder.movieRatingText.setText(String.valueOf(movie.getVoteAverage()) + "/10");

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra("movie", movies.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    /*
        Used when the selection criteria is changed and
        the list must be populated with other movies
     */
    public void clearMovies() {
        movies.clear();
        notifyDataSetChanged();
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    class MovieHolder extends RecyclerView.ViewHolder {

        ImageView posterImage;
        TextView movieTitleText;
        TextView movieRatingText;
        RelativeLayout relativeLayout;

        public MovieHolder(View itemView) {
            super(itemView);

            posterImage = itemView.findViewById(R.id.poster_iv);
            movieTitleText = itemView.findViewById(R.id.title_tv);
            movieRatingText = itemView.findViewById(R.id.rating_tv);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_id);
        }
    }

}

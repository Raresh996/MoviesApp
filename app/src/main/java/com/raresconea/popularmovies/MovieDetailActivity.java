package com.raresconea.popularmovies;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.raresconea.popularmovies.adapter.ReviewsRecyclerAdapter;
import com.raresconea.popularmovies.adapter.TrailersRecyclerAdapter;
import com.raresconea.popularmovies.api.MovieDBClient;
import com.raresconea.popularmovies.api.MovieDBService;
import com.raresconea.popularmovies.data.FavMoviesContract;
import com.raresconea.popularmovies.model.Movie;
import com.raresconea.popularmovies.model.Review;
import com.raresconea.popularmovies.model.ReviewResponse;
import com.raresconea.popularmovies.model.Trailer;
import com.raresconea.popularmovies.model.TrailerResponse;
import com.raresconea.popularmovies.utilities.NetworkUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.moviePoster_iv)
    ImageView moviePoster;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.releaseDate_tv)
    TextView releaseDate;

    @BindView(R.id.rating)
    RatingBar rating;

    @BindView(R.id.voteAverage_tv)
    TextView voteAverage;

    @BindView(R.id.overview_tv)
    TextView overview;

    @BindView(R.id.trailersRecycleView)
    RecyclerView trailersRecycleView;

    @BindView(R.id.reviewsRecycleView)
    RecyclerView reviewsRecycleView;

    @BindView(R.id.favoriteFab_id)
    FloatingActionButton favoriteFab;

    private boolean isFavorite;
    private boolean isInternetConnection;

    private TrailersRecyclerAdapter trailersRecyclerAdapter;
    private ReviewsRecyclerAdapter reviewsRecyclerAdapter;

    private Movie movie;

    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    private static final int MOVIE_LOADER_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
            Set a font to the title of the collapsing toolbar
            so the title of the movie can be seen entirely
         */
        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);
        collapsingToolbarLayout.setExpandedTitleTypeface(font);

        /*
            If a movie was passed via intent store
            it in movie object
         */
        if (getIntent().hasExtra("movie")) {
            movie = getIntent().getParcelableExtra("movie");

            checkIfFavorite();

            //  Set the adapter for the trailers
            trailersRecycleView.setHasFixedSize(true);
            LinearLayoutManager trailersLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            trailersRecycleView.setLayoutManager(trailersLayoutManager);
            trailersRecyclerAdapter = new TrailersRecyclerAdapter(getApplicationContext());
            trailersRecycleView.setAdapter(trailersRecyclerAdapter);

            //  Set the adapter for the reviews
            reviewsRecycleView.setHasFixedSize(true);
            LinearLayoutManager reviewsLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            reviewsRecycleView.setLayoutManager(reviewsLayoutManager);
            reviewsRecyclerAdapter = new ReviewsRecyclerAdapter(getApplicationContext());
            reviewsRecycleView.setAdapter(reviewsRecyclerAdapter);

        }

        registerInternetReceiver();
    }

    //  Register the receiver for internet connection
    private void registerInternetReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = NetworkUtil.getConnectivityStatusString(context);

                if ((status.equals(getResources().getString(R.string.no_internet)))) {
                    isInternetConnection = false;
                } else {
                    isInternetConnection = true;
                }


                populateViews();
            }
        };

        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(receiver, intentFilter);
    }

    /*
        Search in the database to see if this movie
        is marked as favorite so the corect image
        can be set in the floating action button
     */
    private void checkIfFavorite() {
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
    }

    @OnClick(R.id.favoriteFab_id)
    public void clickOnFavorite(View view) {
        if (isFavorite) {
            favoriteFab.setImageResource(R.drawable.whiteheart);
            removeFromFavorite();
        } else {
            favoriteFab.setImageResource(R.drawable.redheart);
            addToFavorite();
        }

        isFavorite = !isFavorite;
    }

    //  Add this movie into the database of favorite movies
    private void addToFavorite() {

        //  Handle the insert request asynchronous
        AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                super.onInsertComplete(token, cookie, uri);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();
                isFavorite = true;
            }
        };

        ContentValues contentValues = new ContentValues();

        contentValues.put(FavMoviesContract.FavMoviesEntry._ID, movie.getId());
        contentValues.put(FavMoviesContract.FavMoviesEntry.COLUMN_TITLE, movie.getOriginalTitle());
        contentValues.put(FavMoviesContract.FavMoviesEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        contentValues.put(FavMoviesContract.FavMoviesEntry.COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(FavMoviesContract.FavMoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        contentValues.put(FavMoviesContract.FavMoviesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());

        asyncQueryHandler.startInsert(-1, null, FavMoviesContract.FavMoviesEntry.CONTENT_URI, contentValues);
    }

    //  Remove this movie from the database of favorite movies
    private void removeFromFavorite() {
        //  Handle the delete asynchronous
        AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                super.onInsertComplete(token, cookie, uri);
            }

            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                super.onDeleteComplete(token, cookie, result);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();
                isFavorite = false;
            }
        };

        Uri uri = ContentUris.withAppendedId(FavMoviesContract.FavMoviesEntry.CONTENT_URI, movie.getId());

        asyncQueryHandler.startDelete(-1, null, uri,
                "_id = ?",
                new String[]{String.valueOf(movie.getId())});
    }

    /*
        Set the image and the text for all the components
        of the layout
     */
    private void populateViews() {
        collapsingToolbarLayout.setTitle(movie.getOriginalTitle());

        //  Set the image and download the trailers and the reviews
        // only if there is internet connection
        if (isInternetConnection) {
            String moviePosterPath = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();

            //  If image is not null then use it to display the image
            //  otherwise download the image using it's path

            Glide
                    .with(getApplicationContext())
                    .load(moviePosterPath)
                    .into(moviePoster);

            loadTrailers();
            loadReviews();
        }

        releaseDate.setText(movie.getReleaseDate());

        rating.setRating((int) Math.round(movie.getVoteAverage()));
        voteAverage.setText(movie.getVoteAverage() + "/10");

        overview.setText(movie.getOverview());


    }

    /*
        Download all the trailers of this movie
        and set them in adapter
     */
    private void loadTrailers() {
        MovieDBService service = MovieDBClient.getClient().create(MovieDBService.class);
        service.getMovieTrailers(movie.getId(), BuildConfig.MOVIE_DB_API_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Response<TrailerResponse>, List<Trailer>>() {
                    @Override
                    public List<Trailer> apply(Response<TrailerResponse> trailerResponse) throws Exception {
                        return trailerResponse.body().getTrailers();
                    }
                })
                .subscribe(trailers -> trailersRecyclerAdapter.setTrailers(trailers));
    }

    /*
        Download all the reviews about this movie
     */
    private void loadReviews() {
        MovieDBService service = MovieDBClient.getClient().create(MovieDBService.class);
        service.getMovieReviews(movie.getId(), BuildConfig.MOVIE_DB_API_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Response<ReviewResponse>, List<Review>>() {
                    @Override
                    public List<Review> apply(Response<ReviewResponse> reviewResponse) throws Exception {
                        return reviewResponse.body().getReviews();
                    }
                })
                .subscribe(reviews -> reviewsRecyclerAdapter.setReviews(reviews));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        Check the internet connection
     */
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onResume(){
        super.onResume();

        if (movie != null){
            populateViews();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this){

            //  Cursor to hold all favorite movies
            Cursor mFavMoviesData = null;

            @Override
            protected void onStartLoading() {
                // Force a new load
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    String stringId = Integer.toString(movie.getId());
                    Uri uri = ContentUris.withAppendedId(FavMoviesContract.FavMoviesEntry.CONTENT_URI, movie.getId());

                    return getContentResolver().query(uri,
                            null,
                            "_id = ?",
                            new String[]{movie.getId() + ""},
                            null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mFavMoviesData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        /*
            isFavorite is true if the movie is already in the db
            or false if it is not
         */
        isFavorite = false;

        /*
            If the cursor has data then it means that our movie
            is in the database
         */
        if(cursor.getCount() > 0){
            isFavorite = true;
        }
        setImageInFloatingActionButton(isFavorite);
    }

    private void setImageInFloatingActionButton(boolean isFavorite) {
        int id;

        if (isFavorite){
            id = R.drawable.redheart;
        } else {
            id = R.drawable.whiteheart;
        }

        favoriteFab.setImageResource(id);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}

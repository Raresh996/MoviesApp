package com.raresconea.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.raresconea.popularmovies.adapter.MoviesRecyclerAdapter;
import com.raresconea.popularmovies.api.MovieDBClient;
import com.raresconea.popularmovies.api.MovieDBService;
import com.raresconea.popularmovies.data.FavMoviesContract;
import com.raresconea.popularmovies.model.Movie;
import com.raresconea.popularmovies.model.MovieResponse;
import com.raresconea.popularmovies.preference.SettingsActivity;
import com.raresconea.popularmovies.utilities.NetworkUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String RECYCLER_POSITION = "recycler_position";

    @BindView(R.id.moviesRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.nowifi_tv)
    TextView noWifiTv;

    @BindView(R.id.nowifi_iv)
    ImageView noWifiIv;

    private MoviesRecyclerAdapter moviesRecyclerAdapter;

    SharedPreferences preferences;

    String[] retrofitCriterias;
    String[] displayedCriterias;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FAVORITES_LOADER_ID = 0;

    private boolean executeLoader;
    private boolean isFavoriteSelected;
    private boolean isInternetConnection;
    private boolean isLoaderCreated;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        preferences =  PreferenceManager.getDefaultSharedPreferences(this);

        retrofitCriterias = getResources().getStringArray(R.array.selectionCriteriaForRetrofit);
        displayedCriterias = getResources().getStringArray(R.array.selectionCriterias);

        /*
            When the phone orientation is PORTRAIT then the
            number of rows which offer a nice layout look is 2
            and when the orientations is Landscape
            the layout can support a number of rows of 4
         */
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setHasFixedSize(true);

        moviesRecyclerAdapter = new MoviesRecyclerAdapter(getApplicationContext());
        recyclerView.setAdapter(moviesRecyclerAdapter);

        PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);

        firstSortCheck();

        registerInternetReceiver();

    }

    //  Check what sort order is selected
    // when the app start
    // (for the case when app starts with
    //  favorites sort order selected)
    private void firstSortCheck() {
        String sortOrder = preferences.getString(
                this.getString(R.string.sort_criteria_key),
                this.getString(R.string.top_rated_criteria));
        if (!sortOrder.equals(getResources().getString(R.string.top_rated_criteria)) &&
                !sortOrder.equals(getResources().getString(R.string.popular_criteria))) {
            isFavoriteSelected = true;
        }
    }

    //  When there is internet connection the recyclerview
    //  must be visible
    public void viewsForInternetConnection(){
        recyclerView.setVisibility(View.VISIBLE);
        noWifiIv.setVisibility(View.GONE);
        noWifiTv.setVisibility(View.GONE);
        checkSelectedCriteria();
    }

    //  When there is not internet connection
    //  and the selected criteria is not Favorite
    //  tv and iv that ask the user to connect to
    //  internet must be visible
    public void viewsForNoInternetConnection(){
            recyclerView.setVisibility(View.GONE);
            noWifiIv.setVisibility(View.VISIBLE);
            noWifiTv.setVisibility(View.VISIBLE);
            checkSelectedCriteria();
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


                setViews();
            }
        };

        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(receiver, intentFilter);
    }

    /*
        Check which is the selected criteria for sorting
        the movies and execute the loadMovies()
        method with the selected criteria as String parameter
     */
    private void checkSelectedCriteria() {

        clearDataInList();

        String sortOrder = preferences.getString(
                this.getString(R.string.sort_criteria_key),
                this.getString(R.string.top_rated_criteria)
        );

        //  Store the String criteria in the form needed
        String selectedCriteria = "";

        if (sortOrder.equals(getResources().getString(R.string.top_rated_criteria))){
            selectedCriteria = retrofitCriterias[0];

            //  Set the title of the action bar to Top Rated
            setTitle(getResources().getString(R.string.top_rated_criteria));
        } else if (sortOrder.equals(getResources().getString(R.string.popular_criteria))){
            selectedCriteria = retrofitCriterias[1];

            //  Set the title of the action bar to Most Popular
            setTitle(getResources().getString(R.string.popular_criteria));
        } else {
            setTitle(getResources().getString(R.string.favorites_criteria));

            //  Start the load to return the cursor with data about movies
            executeLoader = true;

            //  If the loader already exists then restart it
            //  otherwise init it
            if (isLoaderCreated) {
                getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
            } else {
                isLoaderCreated = true;
                getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
            }

            return;
        }

         /*
            If there is internet connection then start
            downloading the movies
         */
//        if(isNetworkAvailable(getApplicationContext())) {
//            loadMovies(selectedCriteria);
//        }

        if(isInternetConnection) {
            loadMovies(selectedCriteria);
        }

    }

    private void clearDataInList() {
        moviesRecyclerAdapter.clearMovies();
    }

    /*
        Download all the movies found with this
        criteria and set them in the adapter
     */
    private void loadMovies(String criteria) {

        MovieDBService service = MovieDBClient.getClient().create(MovieDBService.class);
        service.getMoviesByCriteria(criteria, BuildConfig.MOVIE_DB_API_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Response<MovieResponse>, List<Movie>>() {
                    @Override
                    public List<Movie> apply(Response<MovieResponse> movieResponse) throws Exception {
                        return movieResponse.body().getMovies();
                    }
                })
                .subscribe(movies -> moviesRecyclerAdapter.setMovies(movies));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
        Check the internet connection
     */
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s){
        clearDataInList();
        String selected = sharedPreferences.getString(s, "");

        //  If the favorites criteria is selected make isFavoriteSelected true
        //  otherwise make it false
        if (selected.equals(getResources().getString(R.string.favorites_criteria))){
            isFavoriteSelected = true;
        } else {
            isFavoriteSelected = false;
        }

        checkSelectedCriteria();

        //  If the selection criteria in changed
        //  then the position of the recyclerview must
        //  be at top
        recyclerView.getLayoutManager().scrollToPosition(0);

    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (moviesRecyclerAdapter.getItemCount() == 0) {
            setViews();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //  Save the state of the recyclerview so that
        //  the user will be at the same position if
        //  he/she goes to DetailActivity and then back to MainActivity
        outState.putParcelable(RECYCLER_POSITION, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //  If there is any state saved the
        //  restore it
        if (savedInstanceState != null) {
            Parcelable savedState = savedInstanceState.getParcelable(RECYCLER_POSITION);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedState);
        }
    }

    //  Check if there is internet connection or
    //  if the favorite sort is checked and display
    //  the views for the matched situation
    private void setViews() {
        if (!isInternetConnection && !isFavoriteSelected) {
            viewsForNoInternetConnection();
        } else {
            viewsForInternetConnection();
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
                    return getContentResolver().query(FavMoviesContract.FavMoviesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
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
        //  Start the asynctask that will populate a list with movies
        //  and send it to the adapter
        if (executeLoader) {
            executeLoader = false;
            new MyAsyncTask(cursor).execute();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        moviesRecyclerAdapter.clearMovies();
    }


    /*
        Used to retrieve the data from the cursor returned by
        the loader.
        Data will be store in a list and the sent to the adapter
        to be displayed
     */
    class MyAsyncTask extends AsyncTask<Void, Movie, String> {

        private Cursor cursor;
        List<Movie> movies;

        public MyAsyncTask(Cursor cursor) {
            this.cursor = cursor;
            movies = new ArrayList<>();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (cursor.moveToFirst()){
                do {
                    Movie m = new Movie();
                    m.setId(cursor.getInt(0));
                    m.setOriginalTitle(cursor.getString(1));
                    m.setPosterPath(cursor.getString(2));
                    m.setOverview(cursor.getString(3));
                    m.setVoteAverage(cursor.getDouble(4));
                    m.setReleaseDate(cursor.getString(5));
                    publishProgress(m);
                } while (cursor.moveToNext());
            }

            return "Worked";
        }

        @Override
        protected void onProgressUpdate(Movie... values) {
            super.onProgressUpdate(values);
            movies.add(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            moviesRecyclerAdapter.setMovies(movies);
        }
    }
}

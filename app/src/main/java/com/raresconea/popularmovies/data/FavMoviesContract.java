package com.raresconea.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Rares on 3/17/2018.
 */

public class FavMoviesContract {

    // The authority, this is how the code knows which Content Provider to access
    public static final String AUTHORITY = "com.raresconea.popularmovies";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Possible paths for accessing data in this contract
    public static final String PATH_FAVORITES = "favorites";

    /* Defines the contents of the favorites table */
    public static final class FavMoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        //  Name of the table
        public static final String TABLE_NAME = "favorites";

        //  Name of the column that stores the title of the movie
        public static final String COLUMN_TITLE = "title";

        //  Name of the column that stores the poster image of the movie
        public static final String COLUMN_POSTER_PATH = "poster_path";

        //  Name of the column that stores the overview of the movie
        public static final String COLUMN_OVERVIEW = "overview";

        //  Name of the column that stores the vote average of the movie
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        //  Name of the column that stores the release date of the movie
        public static final String COLUMN_RELEASE_DATE = "release_date";

    }
}

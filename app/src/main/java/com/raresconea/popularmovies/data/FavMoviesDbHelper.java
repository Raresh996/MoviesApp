package com.raresconea.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raresconea.popularmovies.data.FavMoviesContract.FavMoviesEntry;
/**
 * Created by Rares on 3/17/2018.
 */

public class FavMoviesDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "favoritesDb.db";

    private static final int VERSION = 1;

    public FavMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE = "CREATE TABLE "  + FavMoviesEntry.TABLE_NAME + " (" +
                FavMoviesEntry._ID                + " INTEGER PRIMARY KEY, " +
                FavMoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                FavMoviesEntry.COLUMN_RELEASE_DATE    + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("ALTER TABLE " + FavMoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

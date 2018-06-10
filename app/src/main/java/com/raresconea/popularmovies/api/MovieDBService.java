package com.raresconea.popularmovies.api;

import com.raresconea.popularmovies.model.MovieResponse;
import com.raresconea.popularmovies.model.ReviewResponse;
import com.raresconea.popularmovies.model.TrailerResponse;

import java.util.List;
import java.util.Observer;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Rares on 3/10/2018.
 */

public interface MovieDBService {

    /*
        Method used by retrofit to retrieve
        all the movies by a criteria of
        selection
     */
    @GET("movie/{criteria}")
    Observable<Response<MovieResponse>> getMoviesByCriteria( @Path("criteria") String criteria, @Query("api_key") String apiKey);

    /*
        Get available trailers for a specified movie
     */
    @GET("movie/{movieId}/videos")
    Observable<Response<TrailerResponse>> getMovieTrailers(@Path("movieId") int movieId, @Query("api_key") String apiKey);

    /*
        Get available reviews for a specified movie
     */
    @GET("movie/{movieId}/reviews")
    Observable<Response<ReviewResponse>> getMovieReviews(@Path("movieId") int movieId, @Query("api_key") String apiKey);


}



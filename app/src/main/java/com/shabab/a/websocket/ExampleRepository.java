package com.shabab.a.websocket;

/**
 * Created by a on 8/22/2017.
 */
import io.reactivex.Flowable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Naik on 24.02.17.
 */
public interface ExampleRepository {

    @POST("hello-convert-and-send")
    Flowable<Void> sendRestEcho(@Query("msg") String message);
}
package com.shabab.a.websocket;

/**
 * Created by a on 8/22/2017.
 */
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Naik on 24.02.17.
 */
public class RestClient {

    public static final String ANDROID_EMULATOR_LOCALHOST = "192.168.1.55";
    public static final String SERVER_PORT = "8080";

    private static RestClient instance;
    private static final Object lock = new Object();

    public static RestClient getInstance() {
        RestClient instance = RestClient.instance;
        if (instance == null) {
            synchronized (lock) {
                instance = RestClient.instance;
                if (instance == null) {
                    RestClient.instance = instance = new RestClient();
                }
            }
        }
        return instance;
    }

    private final ExampleRepository mExampleRepository;

    private RestClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://" + ANDROID_EMULATOR_LOCALHOST + ":" + SERVER_PORT + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mExampleRepository = retrofit.create(ExampleRepository.class);
    }

    public ExampleRepository getExampleRepository() {
        return mExampleRepository;
    }
}
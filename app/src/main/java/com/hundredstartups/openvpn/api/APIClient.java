package com.hundredstartups.openvpn.api;

import androidx.annotation.NonNull;

import com.hundredstartups.openvpn.cache.UserAccountManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient implements Interceptor {

//    private final String BASE_URL = "https://proxyprivat.com/VPN/Api/";
    private final String BASE_URL = "https://android-vpn.agium.com/Api/";

    private static Retrofit retrofit = null;
    private static OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder();
    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    public Retrofit getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient())
                .build();
        return retrofit;
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            okHttpClient.connectTimeout(30, TimeUnit.SECONDS);
            okHttpClient.readTimeout(60, TimeUnit.SECONDS);
            okHttpClient.writeTimeout(8, TimeUnit.MINUTES);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClient.addInterceptor(interceptor);
            okHttpClient.addInterceptor(this);
            return okHttpClient.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();
        request.addHeader("content-type", "application/json");
        request.addHeader("Token", UserAccountManager.INSTANCE.getToken());
        request.addHeader("UserId", UserAccountManager.INSTANCE.getUserId());
        return chain.proceed(request.build());
    }
}

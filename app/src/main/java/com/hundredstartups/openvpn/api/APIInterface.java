package com.hundredstartups.openvpn.api;

import com.hundredstartups.openvpn.model.Empty;
import com.hundredstartups.openvpn.model.GoogleSignInRequestBody;
import com.hundredstartups.openvpn.model.Response;
import com.hundredstartups.openvpn.model.VpnData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIInterface {

    @POST("LoginGoogle.php")
    Call<Void> sendGoogleToken(@Body GoogleSignInRequestBody googleSignInRequestBody);

    @POST("Exit.php")
    Call<Void> logout(@Body Empty empty);

    @POST("GetVpnData.php")
    Call<VpnData> getVpnData(@Body Empty empty);

    @POST("Reward.php")
    Call<VpnData> getReward(@Body Empty empty);
}

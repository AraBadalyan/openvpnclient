package com.hundredstartups.openvpn.model;

public class GoogleSignInRequestBody {

    String id_token;
    String device_token_id;

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public String getDevice_token_id() {
        return device_token_id;
    }

    public void setDevice_token_id(String device_token_id) {
        this.device_token_id = device_token_id;
    }
}

package com.portfolio.Stackfolio.dto.auth;

public class SigninResponse {
    String email;
    String username;
    boolean signinSuccessful;
    String token;
    public SigninResponse(String email, String username, boolean signinSuccessful,String token ) {
        this.email=email;
        this.username=username;
        this.signinSuccessful=signinSuccessful;
        this.token=token;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public boolean getSigninSuccessful() {
        return signinSuccessful;
    }
    public void setSigninSuccessful(boolean signinSuccessful) {
        this.signinSuccessful = signinSuccessful;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}

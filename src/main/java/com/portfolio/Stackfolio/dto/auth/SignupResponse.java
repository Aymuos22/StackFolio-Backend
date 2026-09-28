package com.portfolio.Stackfolio.dto.auth;

public class SignupResponse {
    private Long id;
    private String token;
    private String username;
    private String email;

    public SignupResponse(Long id,String username, String email,String token ){
        this.id=id;
        this.username=username;
        this.email=email;
        this.token=token;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}

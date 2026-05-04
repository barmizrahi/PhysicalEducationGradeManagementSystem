package com.pe.grademanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Google OAuth authentication request.
 * Contains the authorization code received from Google OAuth flow.
 */
public class GoogleAuthRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    private String redirectUri;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String code) {
        this.code = code;
    }

    public GoogleAuthRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}

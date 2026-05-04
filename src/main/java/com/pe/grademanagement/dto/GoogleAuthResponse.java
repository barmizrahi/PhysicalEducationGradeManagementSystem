package com.pe.grademanagement.dto;

/**
 * DTO for Google OAuth authentication response.
 * Contains the JWT token and user information returned to the frontend.
 */
public class GoogleAuthResponse {

    private String token;
    private UserInfo user;

    public GoogleAuthResponse() {
    }

    public GoogleAuthResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * Nested class for user information.
     */
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private String picture;

        public UserInfo() {
        }

        public UserInfo(Long id, String email, String name, String picture) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }
    }
}

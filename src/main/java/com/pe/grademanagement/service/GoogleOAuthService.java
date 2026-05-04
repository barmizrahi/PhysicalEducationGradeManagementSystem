package com.pe.grademanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service for handling Google OAuth 2.0 authentication flow.
 * Exchanges authorization codes for user information and validates OAuth tokens.
 */
@Service
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;

    public GoogleOAuthService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Exchanges an authorization code for user information from Google.
     *
     * @param authorizationCode The authorization code received from Google OAuth
     * @param redirectUri The redirect URI used in the OAuth flow (optional, uses default if null)
     * @return Map containing user profile information (sub, name, email, picture)
     * @throws RuntimeException if the OAuth exchange fails
     */
    public Map<String, Object> exchangeCodeForUserInfo(String authorizationCode, String redirectUri) {
        logger.debug("Exchanging authorization code for access token");

        // Use provided redirectUri or fall back to configured one
        String effectiveRedirectUri = (redirectUri != null && !redirectUri.isEmpty()) ? redirectUri : this.redirectUri;
        logger.debug("Using redirect URI: {}", effectiveRedirectUri);

        // Step 1: Exchange authorization code for access token
        String accessToken = exchangeCodeForAccessToken(authorizationCode, effectiveRedirectUri);

        // Step 2: Use access token to get user information
        return getUserInfo(accessToken);
    }

    /**
     * Exchanges authorization code for access token.
     *
     * @param authorizationCode The authorization code from Google
     * @param redirectUri The redirect URI to use for token exchange
     * @return The access token
     * @throws RuntimeException if the exchange fails
     */
    private String exchangeCodeForAccessToken(String authorizationCode, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                logger.debug("Successfully obtained access token");
                return accessToken;
            } else {
                logger.error("Failed to exchange code for token: {}", response.getStatusCode());
                throw new RuntimeException("Failed to exchange authorization code for access token");
            }
        } catch (Exception e) {
            logger.error("Error exchanging authorization code: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth token exchange failed: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves user information from Google using an access token.
     *
     * @param accessToken The access token
     * @return Map containing user profile information
     * @throws RuntimeException if the request fails
     */
    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("Successfully retrieved user info for email: {}", response.getBody().get("email"));
                return response.getBody();
            } else {
                logger.error("Failed to get user info: {}", response.getStatusCode());
                throw new RuntimeException("Failed to retrieve user information");
            }
        } catch (Exception e) {
            logger.error("Error retrieving user info: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user information: " + e.getMessage(), e);
        }
    }

    /**
     * Validates an OAuth token by attempting to retrieve user information.
     *
     * @param accessToken The access token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String accessToken) {
        try {
            getUserInfo(accessToken);
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts user profile information from Google OAuth response.
     *
     * @param userInfo The user info map from Google
     * @return Map with standardized user profile fields
     */
    public Map<String, String> extractUserProfile(Map<String, Object> userInfo) {
        String googleId = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        logger.debug("Extracted user profile - Email: {}, Name: {}", email, name);

        return Map.of(
                "googleId", googleId != null ? googleId : "",
                "email", email != null ? email : "",
                "name", name != null ? name : "",
                "picture", picture != null ? picture : ""
        );
    }
}

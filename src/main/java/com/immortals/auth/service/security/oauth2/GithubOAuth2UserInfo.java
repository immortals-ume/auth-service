package com.immortals.auth.service.security.oauth2;

import java.util.Map;

public class GithubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getProviderId()  { return String.valueOf(attributes.get("id")); }
    @Override public String getName()        { return (String) attributes.get("name"); }
    @Override public String getEmail()       { return (String) attributes.get("email"); }
    @Override public String getImageUrl()    { return (String) attributes.get("avatar_url"); }
}


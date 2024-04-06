package com.ccloud.main.config.jwt;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * JWTToken
 *
 * @author : wangjie
 */
public class JwtToken implements AuthenticationToken {
    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}

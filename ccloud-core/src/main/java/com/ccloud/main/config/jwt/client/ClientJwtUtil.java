package com.ccloud.main.config.jwt.client;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ccloud.main.config.jwt.JwtUtil;
import com.ccloud.main.config.shiro.ShiroJwtAuthorization;
import com.ccloud.main.config.shiro.client.ShiroJwtClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Date;

import static org.apache.shiro.SecurityUtils.getSubject;


/**
 * JWTUtil
 *
 * @author : wangjie
 */
@Configuration
public class ClientJwtUtil implements JwtUtil {

    @Resource
    public ObjectMapper objectMapper;
    @Resource
    public ShiroJwtClientProperties shiroJwtClientProperties;
    @Resource
    public ShiroJwtAuthorization shiroJwtAuthorization;

    /**
     * verify token
     *
     * @param token token
     * @return true or false
     */
    @Override
    public boolean verify(String token, String userId) {
        try {
            String secret = shiroJwtAuthorization.getSecret(userId) == null ? shiroJwtClientProperties.getSecret() : null;
            assert secret != null;
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withSubject(userId)
                    .build();
            verifier.verify(token);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * get user id
     *
     * @return user id
     */
    public String getUserId() {
        try {
            DecodedJWT jwt = JWT.decode(getSubject().getPrincipal().toString());
            return jwt.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get user id
     *
     * @param token jwt token
     * @return user id
     */
    @Override
    public String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * generate token
     *
     * @param userId user identifier
     * @return token
     */
    public String sign(String userId) {
        Date date = new Date(System.currentTimeMillis() + shiroJwtClientProperties.getExpireTime());
        String secret = shiroJwtAuthorization.getSecret(userId) == null ? shiroJwtClientProperties.getSecret() : null;
        assert secret != null;
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return shiroJwtClientProperties.getSignPrefix() + JWT.create()
                .withSubject(userId)
                .withExpiresAt(date)
                .sign(algorithm);
    }
}

package com.dvc.utils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtUtils {
    public static final long JWT_TOKEN_VALIDITY = (long) (60 * 24 * 60 * 1000);

    public static String generateToken(Map<String, Object> claims, String subject, boolean isExpiredInclude,
            String key) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(key);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        if (isExpiredInclude) {
            return Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                    .signWith(signatureAlgorithm, signingKey).compact();
        } else {
            return Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis())).signWith(signatureAlgorithm, signingKey)
                    .compact();
        }
    }

    public static String generateTokenV2(Map<String, Object> claims, String subject, boolean isExpiredInclude,
            String key) throws UnsupportedEncodingException {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(key);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        if (isExpiredInclude) {
            return Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                    .signWith(signatureAlgorithm, key.getBytes("UTF-8")).compact();
        } else {
            return Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .signWith(signatureAlgorithm, key.getBytes("UTF-8")).compact();
        }
    }

    public static boolean isTokenExpired(String token, String key) {
        Date expiration = getAllClaimsFromToken(token, key).getExpiration();
        return expiration.before(new Date());
    }

    public static boolean isTokenValid(String token, String key) {
        Claims claim = getAllClaimsFromToken(token, key);
        if (claim == null)
            return false;
        String body = claim.getSubject();
        if (body == null)
            return false;
        return true;
    }

    public static boolean isTokenValidV2(String token, String key) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException, UnsupportedEncodingException {
        Claims claim = getAllClaimsFromTokenV2(token, key);
        if (claim == null)
            return false;
        String body = claim.getSubject();
        if (body == null)
            return false;
        return true;
    }

    public static String getTokenDataString(String token, String key) throws IOException {
        return getAllClaimsFromToken(token, key).getSubject();
    }

    public static String getTokenDataStringV2(String token, String key) throws IOException {
        return getAllClaimsFromTokenV2(token, key).getSubject();
    }

    public static Map<String, Object> getTokenData(String token, String key) throws IOException {
        return new ObjectMapper().readValue(getAllClaimsFromToken(token, key).getSubject(), Map.class);
    }

    private static Claims getAllClaimsFromTokenV2(String token, String key)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException,
            IllegalArgumentException, UnsupportedEncodingException {
        return Jwts.parser().setSigningKey(key.getBytes("UTF-8")).parseClaimsJws(token).getBody();
    }

    private static Claims getAllClaimsFromToken(String token, String key) {
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(key)).parseClaimsJws(token).getBody();
    }
}

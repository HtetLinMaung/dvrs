package com.dvc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.dvc.factory.DbFactory;
import com.dvc.models.TokenData;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class TokenUtil {

	public static final long JWT_TOKEN_VALIDITY = (long) (60 * 24 * 60 * 1000);
	public static final long JWT_TOKEN_ONE_MIN_VALIDITY = (long) (3 * 60 * 1000);
	private static final String SECRET_KEY = "3ay3ay1032smepapz18acp$0wf";//
	private static final String B_SECRET_KEY = System.getenv("B_SECRET_KEY");
	private static final String QR_TOKEN_KEY = "9&pJ@?G-%@Tw%(wJC]0|E5X0f*";

	/**
	 * This method is used to get username from token
	 * 
	 * @param token token
	 * @return username
	 */
	public static String getTokenData(String token, String type) {
		Claims claim = getAllClaimsFromToken(token, type);
		if (claim != null) {
			return claim.getSubject();
		} else {
			return null;
		}
	}

	public static TokenData getBTokenData(String token) throws JsonMappingException, JsonProcessingException {
		return new ObjectMapper().readValue(getTokenData(token, "b"), TokenData.class);
	}

	/**
	 * This method is used to get the expired date from token
	 * 
	 * @param token token
	 * @return expired date
	 */
	public static Date getExpirationDateFromToken(String token, String type) {
		return getAllClaimsFromToken(token, type).getExpiration();
	}

	/**
	 * This method is used to get claims from token
	 * 
	 * @param token token
	 * @return claims
	 */
	private static Claims getAllClaimsFromToken(String token, String type) {
		try {
			return Jwts.parser()
					.setSigningKey(DatatypeConverter.parseBase64Binary(type.equals("a") ? SECRET_KEY : B_SECRET_KEY))
					.parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			// e.printStackTrace();
			return null;
		} catch (UnsupportedJwtException e) {
			// e.printStackTrace();
			return null;
		} catch (MalformedJwtException e) {
			// e.printStackTrace();
			return null;
		} catch (SignatureException e) {
			// e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method is used to check the token is expired or not
	 * 
	 * @param token token
	 * @return true or false
	 */
	public static Boolean isTokenExpired(String token, String type) {
		try {
			Date expiration = getAllClaimsFromToken(token, type).getExpiration();
			return expiration.before(new Date());
			// long elapsed = expiration.getTime() - new Date().getTime();
			//
			// int hours = (int) Math.floor(elapsed / 3600000);
			//
			// int minutes = (int) Math.floor((elapsed - hours * 3600000) / 60000);
			//
			// int seconds = (int) Math.floor((elapsed - hours * 3600000 - minutes * 60000)
			// / 1000);
			//
			// System.out.format("From %s to %s%n", expiration.getTime(), new
			// Date().getTime());
			//
			// System.out.format("Time elapsed %d milliseconds%n", elapsed);
			//
			// System.out.format("%d hours %d minutes %d seconds%n", hours, minutes,
			// seconds);

		} catch (Exception e) {
			// e.printStackTrace();
			return true;
		}
	}

	// public static Boolean isValidToken(String token) {
	// try {
	// Date expiration = getAllClaimsFromToken(token).getExpiration();
	// long elapsed = expiration.getTime() - new Date().getTime();

	// int hours = (int) Math.floor(elapsed / 3600000);

	// int minutes = (int) Math.floor((elapsed - hours * 3600000) / 60000);

	// // int seconds = (int) Math.floor((elapsed - hours * 3600000 - minutes *
	// 60000)
	// // / 1000);

	// return minutes > Settings.token_time ? true : false;

	// } catch (Exception e) {
	// // e.printStackTrace();
	// return false;
	// }
	// }

	/**
	 * This method is used to generate the token
	 * 
	 * @param subject subject
	 * @return token
	 */
	public static String generateToken(String subject, boolean isExpiredInclude) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, subject, isExpiredInclude);
	}

	/**
	 * This method is used to generate the token
	 * 
	 * @param claims   claim object
	 * @param username username
	 * @return generated token
	 */
	private static String doGenerateToken(Map<String, Object> claims, String subject, boolean isExpiredInclude) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(B_SECRET_KEY);
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

	/**
	 * This method is used to check the token is valid or not
	 * 
	 * @param token token
	 * @param comId company id
	 * @param email email
	 * @return true or false
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@SuppressWarnings("unchecked")
	public static Boolean validToken(String userId, String appId, String token, String type)
			throws JsonMappingException, JsonProcessingException {

		if (!isTokenExpired(token, type)) {
			Map<String, Object> resultMap = new ObjectMapper().readValue(getTokenData(token, type), Map.class);
			if (resultMap != null) {
				return resultMap.get("userId").equals(userId) && resultMap.get("appId").equals(appId);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	// public static String generateSecretKey(String productName) {
	// String format = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	// String encryptedString = encryptPIN(productName + format);
	// return new String(Base64EncryptDecrypt.encode(encryptedString));
	// }

	public static void main(String[] args) {

		try {
			Connection connection = DbFactory.getConnection();
			// PreparedStatement createStatement = connection.prepareStatement(
			// "create table PartnerUser (SysKey bigint not null primary key,CreateDate
			// datetime,ModifiedDate datetime,UserID nvarchar(50),UserName
			// nvarchar(50),RecordStatus SMALLINT default 1,Remark nvarchar(255),Role
			// nvarchar(255),DVRSUserID nvarchar(255),PartnerSyskey bigint not null,T1
			// nvarchar(50),T2 nvarchar(50),T3 nvarchar(50),T4 nvarchar(50),T5
			// nvarchar(50),N1 bigint,N2 bigint,N3 bigint,N4 bigint, N5 bigint)");
			PreparedStatement createStatement = connection.prepareStatement("select * from PartnerUser");
			createStatement.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(generateToken("test", true));
		// System.out.println(isTokenExpired(
		// "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjAxNjQxMzI1LCJpYXQiOjE2MDE2NDEyMDV9.yIn6FI5OxeZO1VyPqm9P6XtqVy9J4OT9Gu23nW1G8PE"));
		// System.out.println(validToken("test", "",
		// "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjAxNjQxMzI1LCJpYXQiOjE2MDE2NDEyMDV9.yIn6FI5OxeZO1VyPqm9P6XtqVy9J4OT9Gu23nW1G8PE"));
		// System.out.println(getUsernameFromToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyX2lkXCI6NDExMTY4LFwiY29tcF9pZFwiOjk0MzExLFwidHlwZVwiOlwiMVwiLFwidG9rZW5cIjpcImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSklVekkxTmlKOS5leUpqYjIxd1gybGtJam81TkRNeE1Td2laWGh3SWpveE5qQXhOamN3TkRVeU5EUTBMQ0psYldGcGJDSTZJbXRvWVhScmFHRjBMblZqYzNOQVoyMWhhV3d1WTI5dEluMC5PaFpSMnRVYS1sWnQzaWVLMFZ0eWh4RnI4Tl90MnM0ajJRMVdFV3hBQjk0XCJ9IiwiaWF0IjoxNjAxNjQwNTQ2fQ.KNqaV6lG_gK-LKWNDz6YvzuAMvYz692LCL6Zlso78NA"));

	}

	public static long getLong(Object o) {
		try {
			return o instanceof String ? Long.parseLong((String) o)
					: o instanceof Integer ? (int) o
							: o instanceof Long ? (long) o
									: o instanceof Double ? (new Double((double) o)).longValue() : 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// public static String encryptPIN(String p) {
	// String ret = "";
	// try {
	// DESedeEncryption myEncryptor = new DESedeEncryption();
	// ret = myEncryptor.encrypt(p);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return ret;
	// }

	public static String getToken(String userId, String appId, boolean isExpired) throws JsonProcessingException {
		Map<String, Object> payload = new HashMap<String, Object>();
		String token = "";
		payload.put("userid", userId);
		payload.put("appid", appId);
		token = TokenUtil.generateToken(new ObjectMapper().writeValueAsString(payload), isExpired);
		return token;
	}

	public static String getBToken(String userId, String appId, String cid, String ctype, String phone,
			String customerID, String apiToken, String username, boolean isExpired) throws JsonProcessingException {
		Map<String, Object> payload = new HashMap<String, Object>();
		String token = "";
		payload.put("userid", userId);
		payload.put("appid", appId);
		payload.put("cid", cid);
		payload.put("ctype", ctype);
		payload.put("phone", phone);
		payload.put("customerID", customerID);
		payload.put("apiToken", apiToken);
		payload.put("username", username);
		token = TokenUtil.generateToken(new ObjectMapper().writeValueAsString(payload), isExpired);
		return token;
	}

	public static String getBToken(Map<String, Object> payload, boolean isExpired) throws JsonProcessingException {
		return TokenUtil.generateToken(new ObjectMapper().writeValueAsString(payload), true);
	}

	public static String generateQRToken(String subject) {
		Map<String, Object> claims = new HashMap<>();
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(QR_TOKEN_KEY);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.signWith(signatureAlgorithm, signingKey).compact();
	}

	public static String getUsernameFromQRToken(String token) {
		Claims claim = getAllClaimsFromQRToken(token);
		if (claim != null) {
			return claim.getSubject();
		} else {
			return null;
		}
	}

	private static Claims getAllClaimsFromQRToken(String token) {
		try {
			return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(QR_TOKEN_KEY)).parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException e) {
			return null;
		} catch (UnsupportedJwtException e) {
			return null;
		} catch (MalformedJwtException e) {
			return null;
		} catch (SignatureException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public static Boolean isQRTokenExpired(String token) {
		try {
			Date expiration = getAllClaimsFromQRToken(token).getExpiration();
			return expiration.before(new Date());
		} catch (Exception e) {
			// e.printStackTrace();
			return true;
		}
	}

	public static String generateQRToken(String cId, String userId) {
		Map<String, String> payload = new HashMap<String, String>();
		String token = "";
		payload.put("userid", userId);
		payload.put("date", Instant.now().toString());
		payload.put("cid", cId);
		token = TokenUtil.generateQRToken(new Gson().toJson(payload));
		return token;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getQRToken(String token) {
		Map<String, String> tokenMap = new HashMap<>();
		Claims claims = getAllClaimsFromQRToken(token);
		if (claims != null && claims.getSubject() != null) {
			tokenMap = new Gson().fromJson(claims.getSubject(), Map.class);
		}
		return tokenMap;
	}

}

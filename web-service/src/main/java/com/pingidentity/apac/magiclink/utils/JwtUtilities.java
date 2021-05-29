package com.pingidentity.apac.magiclink.utils;

import java.security.Key;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;

public class JwtUtilities {

	public static String getLoginHintToken(String clientId, String clientSecret, String subject, String audience) throws Exception {


		JwtClaims requestClaims = new JwtClaims();
		requestClaims.setExpirationTimeMinutesInTheFuture(1);
		requestClaims.setGeneratedJwtId();
		requestClaims.setIssuedAtToNow();
		requestClaims.setIssuer(clientId);
		requestClaims.setNotBeforeMinutesInThePast(1);
		requestClaims.setSubject(subject);
		requestClaims.setAudience(audience);
		
		Key key = new HmacKey(clientSecret.getBytes("UTF-8"));

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(requestClaims.toJson());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setKey(key);
		jws.setDoKeyValidation(false);
		
		String jwt = jws.getCompactSerialization();

		return jwt;

	}
}

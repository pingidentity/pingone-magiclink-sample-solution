package com.pingidentity.pingone.magiclink.utils;

import java.security.Key;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "jwtUtilities")
public class PingOneJwtUtilities {

	@Autowired
	private String pingoneClientId;

	@Autowired
	private String pingoneClientSecret;
	
	public String getLoginHintToken(String subject, String audience) throws Exception {

		JwtClaims requestClaims = new JwtClaims();
		requestClaims.setExpirationTimeMinutesInTheFuture(1);
		requestClaims.setGeneratedJwtId();
		requestClaims.setIssuedAtToNow();
		requestClaims.setIssuer(pingoneClientId);
		requestClaims.setNotBeforeMinutesInThePast(1);
		requestClaims.setSubject(subject);
		requestClaims.setAudience(audience);
		
		Key key = new HmacKey(pingoneClientSecret.getBytes("UTF-8"));

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(requestClaims.toJson());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		jws.setKey(key);
		jws.setDoKeyValidation(false);
		
		String jwt = jws.getCompactSerialization();

		return jwt;

	}
}

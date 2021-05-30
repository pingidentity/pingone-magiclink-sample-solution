package com.pingidentity.pingone.magiclink.otp.impl;

import com.pingidentity.pingone.magiclink.otp.IRequestStorage;
import com.pingidentity.pingone.magiclink.otp.OTLRequest;

public class LocalRequestStorageImpl implements IRequestStorage {

	private final TimeLimitedHashMap<String, OTLRequest> _OTLURLMap = new TimeLimitedHashMap<String, OTLRequest>(200000);
	
	@Override
	public OTLRequest register(String key, OTLRequest value, long expiryInMillis, String ipAddress) {
		return _OTLURLMap.put(key, value, expiryInMillis, ipAddress);
	}

	@Override
	public void remove(Object key) {
		_OTLURLMap.remove(key);

	}

	@Override
	public OTLRequest getRequest(String key, String expectedIpAddress) {
		return _OTLURLMap.get(key, expectedIpAddress);
	}

	@Override
	public boolean isRequestExists(String key) {
		return _OTLURLMap.containsKey(key);
	}

}

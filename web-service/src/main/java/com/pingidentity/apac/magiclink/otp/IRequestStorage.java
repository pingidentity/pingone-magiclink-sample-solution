package com.pingidentity.apac.magiclink.otp;

public interface IRequestStorage {
	public boolean isRequestExists(String key);
	public OTLRequest register(String key, OTLRequest value, long expiryInMillis, String ipAddress);
	public void remove(Object key);
	public OTLRequest getRequest(String key, String expectedIpAddress);
}

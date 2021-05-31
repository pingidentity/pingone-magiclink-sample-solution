package com.pingidentity.pingone.magiclink.otp;

public interface IRequestStorage {
	public boolean isRequestExists(String key);
	public String register(OTLRequest value, long expiryInMillis, String ipAddress);
	public void remove(Object key);
	public OTLRequest getRequest(String key, String expectedIpAddress);
}

package com.pingidentity.apac.magiclink.otp;

public class OneTimeLink {

	private final String status;
	private final String otlLink;
	
	private OneTimeLink(String status, String otlLink)
	{
		this.status = status;
		this.otlLink = otlLink;
	}
	
	public static OneTimeLink getInstance(String status, String otlLink)
	{
		return new OneTimeLink(status, otlLink);
	}
	
	public static OneTimeLink getInstance(String status)
	{
		return new OneTimeLink(status, null);
	}

	public String getStatus() {
		return status;
	}

	public String getOtlLink() {
		return otlLink;
	}
}

package com.pingidentity.pingone.magiclink.utils;

public class HttpResponseObj {

	private final int statusCode;
	private final String responseBody;
	
	public HttpResponseObj(int statusCode, String responseBody)
	{
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getResponseBody() {
		return responseBody;
	}
}

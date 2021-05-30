package com.pingidentity.pingone.magiclink.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CodeNotProvidedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3037337771929156850L;
	
	public CodeNotProvidedException(String msg)
	{
		super(msg);
	}
	
	public CodeNotProvidedException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public int getCode() {
		return 404;
	}

}

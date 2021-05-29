package com.pingidentity.apac.magiclink.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CodeNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3037337771929156850L;
	
	public CodeNotFoundException(String msg)
	{
		super(msg);
	}
	
	public CodeNotFoundException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public int getCode() {
		return 404;
	}

}

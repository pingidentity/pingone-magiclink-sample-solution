package com.pingidentity.magiclinkdemo;

public class OIDCException extends RuntimeException {
    private static final long serialVersionUID = 3037337771929156850L;

    public OIDCException(String msg)
    {
        super(msg);
    }

    public OIDCException(String msg, Throwable t)
    {
        super(msg, t);
    }
}

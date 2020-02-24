package org.whispersystems.textsecuregcm.exceptions;

public class AuthenticationException extends BaseServerResponseException {
    public static final int STATUS_CODE = 401;
    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
        super(401, "Client unauthorized");
    }

    public AuthenticationException(String theMessage) {
        super(401, theMessage);
    }

    public AuthenticationException(String theMessage, Throwable theCause) {
        super(401, theMessage, theCause);
    }

    public AuthenticationException addAuthenticateHeaderForRealm(String theRealm) {
        this.addResponseHeader("WWW-Authenticate", "Basic realm=\"" + theRealm + "\"");
        return this;
    }
}
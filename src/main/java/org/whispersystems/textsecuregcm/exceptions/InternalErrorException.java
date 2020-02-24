package org.whispersystems.textsecuregcm.exceptions;

public class InternalErrorException extends BaseServerResponseException {
    private static final int STATUS_CODE = 500;
    private static final long serialVersionUID = 1L;

    public InternalErrorException(String theMessage) {
        super(STATUS_CODE, theMessage);
    }

    public InternalErrorException(String theMessage, Throwable theCause) {
        super(STATUS_CODE, theMessage, theCause);
    }

    public InternalErrorException(Throwable theCause) {
        super(STATUS_CODE, theCause);
    }
}

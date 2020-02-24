package org.whispersystems.textsecuregcm.exceptions;

public class UnclassifiedServerFailureException extends BaseServerResponseException {
    private static final long serialVersionUID = 1L;

    public UnclassifiedServerFailureException(int theStatusCode, String theMessage) {
        super(theStatusCode, theMessage);
    }
}

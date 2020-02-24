package org.whispersystems.textsecuregcm.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

public abstract class BaseServerResponseException extends RuntimeException {
    private static final Map<Integer, Class<? extends BaseServerResponseException>> ourStatusCodeToExceptionType = new HashMap();
    private static final long serialVersionUID = 1L;
    private List<String> myAdditionalMessages = null;
    private String myResponseBody;
    private Map<String, List<String>> myResponseHeaders;
    private String myResponseMimeType;
    private int myStatusCode;

    public BaseServerResponseException(int theStatusCode, String theMessage) {
        super(theMessage);
        this.myStatusCode = theStatusCode;
    }

    public BaseServerResponseException(int theStatusCode, String... theMessages) {
        super(theMessages != null && theMessages.length > 0 ? theMessages[0] : null);
        this.myStatusCode = theStatusCode;
        if (theMessages != null && theMessages.length > 1) {
            this.myAdditionalMessages = Arrays.asList(Arrays.copyOfRange(theMessages, 1, theMessages.length, String[].class));
        }

    }

    public BaseServerResponseException(int theStatusCode, String theMessage, Throwable theCause) {
        super(theMessage, theCause);
        this.myStatusCode = theStatusCode;
    }

    public BaseServerResponseException(int theStatusCode, Throwable theCause) {
        super(theCause.toString(), theCause);
        this.myStatusCode = theStatusCode;
    }

    public BaseServerResponseException addResponseHeader(String theName, String theValue) {
        Validate.notBlank(theName, "theName must not be null or empty", new Object[0]);
        Validate.notBlank(theValue, "theValue must not be null or empty", new Object[0]);
        if (!this.getResponseHeaders().containsKey(theName)) {
            this.getResponseHeaders().put(theName, new ArrayList());
        }

        ((List)this.getResponseHeaders().get(theName)).add(theValue);
        return this;
    }

    public List<String> getAdditionalMessages() {
        return this.myAdditionalMessages;
    }

    public String getResponseBody() {
        return this.myResponseBody;
    }

    public Map<String, List<String>> getResponseHeaders() {
        if (this.myResponseHeaders == null) {
            this.myResponseHeaders = new HashMap();
        }

        return this.myResponseHeaders;
    }

    public String getResponseMimeType() {
        return this.myResponseMimeType;
    }

    public int getStatusCode() {
        return this.myStatusCode;
    }

    public boolean hasResponseHeaders() {
        return this.myResponseHeaders != null && !this.myResponseHeaders.isEmpty();
    }

    public void setResponseBody(String theResponseBody) {
        this.myResponseBody = theResponseBody;
    }

    public void setResponseMimeType(String theResponseMimeType) {
        this.myResponseMimeType = theResponseMimeType;
    }

    static boolean isExceptionTypeRegistered(Class<?> theType) {
        return ourStatusCodeToExceptionType.values().contains(theType);
    }

    public static BaseServerResponseException newInstance(int theStatusCode, String theMessage) {
        if (ourStatusCodeToExceptionType.containsKey(theStatusCode)) {
            try {
                return (BaseServerResponseException)((Class)ourStatusCodeToExceptionType.get(theStatusCode)).getConstructor(String.class).newInstance(theMessage);
            } catch (InstantiationException var3) {
                throw new InternalErrorException(var3);
            } catch (IllegalAccessException var4) {
                throw new InternalErrorException(var4);
            } catch (IllegalArgumentException var5) {
                throw new InternalErrorException(var5);
            } catch (InvocationTargetException var6) {
                throw new InternalErrorException(var6);
            } catch (NoSuchMethodException var7) {
                throw new InternalErrorException(var7);
            } catch (SecurityException var8) {
                throw new InternalErrorException(var8);
            }
        } else {
            return new UnclassifiedServerFailureException(theStatusCode, theMessage);
        }
    }

    static void registerExceptionType(int theStatusCode, Class<? extends BaseServerResponseException> theType) {
        if (ourStatusCodeToExceptionType.containsKey(theStatusCode)) {
            throw new Error("Can not register " + theType + " to status code " + theStatusCode + " because " + ourStatusCodeToExceptionType.get(theStatusCode) + " already registers that code");
        } else {
            ourStatusCodeToExceptionType.put(theStatusCode, theType);
        }
    }

    static {
        registerExceptionType(401, AuthenticationException.class);
    }
}

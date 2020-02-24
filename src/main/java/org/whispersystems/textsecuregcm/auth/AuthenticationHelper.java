package org.whispersystems.textsecuregcm.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.exceptions.AuthenticationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.List;

public class AuthenticationHelper {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationHelper.class);

    private final ConfigurableJWTProcessor jwtProcessor;
    private JWKSource keySource;
    private final JWSAlgorithm expectedJWSAlg;
    private final JWSKeySelector keySelector;
    //private URI userInfoEndpoint;

    public AuthenticationHelper() {
        jwtProcessor = new DefaultJWTProcessor();

        try {
            keySource = new RemoteJWKSet(new URL("https://movi-keycloak.apps.osc.fokus.fraunhofer.de/auth/realms/MOVI/protocol/openid-connect/certs"));
            //userInfoEndpoint = new URI("https://ehealth-ask.fokus.fraunhofer.de/auth/realms/AsK/protocol/openid-connect/userinfo");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // The expected JWS algorithm of the access tokens (agreed out-of-band)
        expectedJWSAlg = JWSAlgorithm.RS256;

        // Configure the JWT processor with a key selector to feed matching public
        // RSA keys sourced from the JWK set URL
        keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
    }

    public boolean tokenRegistration(String authHeader, String kvnr) {
        if (authHeader == null) {
            // "Authorization" header is missing
            logger.warn("No Authorization Header found! Aborting ...");
            throw new AuthenticationException("Authorization Header Missing");
        } else {
            if (!validateToken(authHeader, kvnr)) {
                throw new AuthenticationException("Invalid parameter of claim set");
            }
            if (!verifySign(authHeader)) {
                throw new AuthenticationException("Authorization Header Missing");
            }
            // There is a chance that we will have a proper access token
            /*try {
                JWTClaimsSet claimsSet = validateToken(authHeader);
                if (!verifySign(authHeader)) {
                    throw new AuthenticationException("Authorization Header Missing");
                }

                logger.info("Nice. Let's continue with the other Interceptors");
                return true;
            } catch (AuthenticationException e) {
                throw new AuthenticationException(e.getMessage());
            }*/
            return true;
        }
    }

    /**
     * @param token
     * @return
     * @throws MalformedURLException
     * @throws ParseException
     * @throws BadJOSEException
     * @throws JOSEException
     */
    private boolean validateToken(String token, String kvnr) throws AuthenticationException {
        try {
            // Process the token
            SecurityContext ctx = null; // optional context parameter, not required here
            JWTClaimsSet claimsSet = jwtProcessor.process(token, ctx);

            if (!claimsSet.getStringClaim("azp").equals("signal")) {
                throw new BadJWTException("Invalid cid/azp");
            }

            if (!claimsSet.getStringClaim("iss").equals("https://movi-keycloak.apps.osc.fokus.fraunhofer.de/auth/realms/MOVI")) {
                throw new BadJWTException("Invalid iss");
            }

            if (!claimsSet.getStringClaim("uid").equals(kvnr)) {
                throw new BadJWTException("Invalid patient id");
            }

            return true;
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    private boolean verifySign(String token) {
        SignedJWT jwt;
        boolean signed = false;
        try {
            jwt = SignedJWT.parse(token);
            if (!jwt.getHeader().getAlgorithm().getName().equals("RS256")) {
                System.out.println("JWT algorithm is not correct! " + jwt.getHeader().getAlgorithm().getName());
                throw new BadJWTException("Invalid JWT algorithm");
            }
            if (!jwt.getHeader().getType().getType().equals("JWT")) {
                System.out.println("JWT type is not correct! " + jwt.getHeader().getType().getType());
                throw new BadJWTException("Invalid JWT type");
            }
            JWKSet jwkSet = getJWKset();
            if (jwkSet != null) {
                List<JWK> matches = new JWKSelector(
                        new JWKMatcher.Builder()
                                .keyType(KeyType.RSA)
                                .keyID(getKeyID(jwkSet))
                                .build()
                ).select(jwkSet);

                JWK jwk = matches.get(0);

                if (!jwt.getHeader().getKeyID().equals(jwk.getKeyID())) {
                    throw new BadJWTException("Invalid Key ID");
                }

                RSAPublicKey rsaPublicJWK = ((RSAKey) JWK.parse(jwk.toJSONObject())).toRSAPublicKey();

                JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);

                if (jwt.verify(verifier)) {
                    System.out.println("JWS is verified!");
                    signed = true;
                } else {
                    System.out.println("JWS is NOT verified!");
                    signed = false;
                }
            } else System.out.println("Something goes wrong: JWK Set == null!");
        } catch (ParseException | JOSEException | BadJWTException e) {
            e.printStackTrace();
        }
        return signed;
    }

    private JWKSet getJWKset() {
        // HTTP connect timeout in milliseconds
        int connectTimeout = 100;
        // HTTP read timeout in milliseconds
        int readTimeout = 100;
        // JWK set size limit, in bytes
        int sizeLimit = 10000;
        JWKSet jwkSet = null;
        // Load JWK set from URL
        try {
            jwkSet = JWKSet.load(new URL("https://movi-keycloak.apps.osc.fokus.fraunhofer.de/auth/realms/MOVI/protocol/openid-connect/certs"),
                    connectTimeout, readTimeout, sizeLimit);
            System.out.println("JWK Set: " + jwkSet.getKeys().get(0).getKeyID());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jwkSet;
    }

    private String getKeyID(JWKSet jwkSet) {
        return jwkSet.getKeys().get(0).getKeyID();
    }
}

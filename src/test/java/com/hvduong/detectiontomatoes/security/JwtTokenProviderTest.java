package com.hvduong.detectiontomatoes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "TestSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 3600000L); // 1 hour
    }

    @Test
    void testGenerateTokenAndGetUsername() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        String username = jwtTokenProvider.getUsernameFromJWT(token);
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken_Success() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                Collections.emptyList()
        );

        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_Expired() {
        // Create an expired token manually
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", -1000L); // Negative expiration
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                Collections.emptyList()
        );

        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_Malformed() {
        String malformedToken = "this.is.not_a_valid_jwt_token";
        
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_InvalidSignature() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "pwd", Collections.emptyList());
        String token = jwtTokenProvider.generateToken(authentication);

        // Modify the signature
        String invalidSignatureToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalid_signature";

        boolean isValid = jwtTokenProvider.validateToken(invalidSignatureToken);
        assertFalse(isValid);
    }
}

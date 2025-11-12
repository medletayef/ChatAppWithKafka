package com.example.chatapp.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {
    //    private final JWTParser jwtParser;
    //    private final UserDetailsService userDetailsService;
    //
    //    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    //    private String jwtKey;
    //
    //    public TokenProvider(JWTParser jwtParser, UserDetailsService userDetailsService) {
    //        this.jwtParser = jwtParser;
    //        this.userDetailsService = userDetailsService;
    //    }
    //
    //    public Authentication getAuthentication(String token) throws ParseException {
    //        JWTClaimsSet claims = jwtParser.parse(token).getJWTClaimsSet();
    //        String username = claims.getSubject();
    //
    //        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    //        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    //    }
    //
    //    public boolean validateToken(String token) {
    //        // verifies signature, expiration, etc.
    //        ...
    //    }
}

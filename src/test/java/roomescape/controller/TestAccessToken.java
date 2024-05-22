package roomescape.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.auth.JwtTokenProvider;

@Component
public class TestAccessToken {
    private static final String ADMIN = "admin@test.com";
    private static final String USER = "user@test.com";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public String getAdminToken() {
        return jwtTokenProvider.createToken(ADMIN);
    }

    public String getUserToken() {
        return jwtTokenProvider.createToken(USER);
    }

    public String getUserToken(String email) {
        return jwtTokenProvider.createToken(email);
    }
}

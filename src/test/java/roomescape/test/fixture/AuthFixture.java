package roomescape.test.fixture;

import roomescape.dto.request.LoginRequest;

public class AuthFixture {

    public static LoginRequest createTokenRequestDto(String email, String password) {
        return new LoginRequest(email, password);
    }
}

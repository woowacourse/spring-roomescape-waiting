package roomescape.test.fixture;

import roomescape.dto.request.loginRequest;

public class AuthFixture {

    public static loginRequest createTokenRequestDto(String email, String password) {
        return new loginRequest(email, password);
    }
}

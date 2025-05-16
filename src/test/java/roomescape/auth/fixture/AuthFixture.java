package roomescape.auth.fixture;

import roomescape.dto.request.TokenRequestDto;

public class AuthFixture {

    public static TokenRequestDto createTokenRequestDto(String email, String password) {
        return new TokenRequestDto(email, password);
    }
}

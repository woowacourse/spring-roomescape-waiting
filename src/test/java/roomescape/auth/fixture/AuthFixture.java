package roomescape.auth.fixture;

import roomescape.auth.domain.dto.TokenRequestDto;

public class AuthFixture {

    public static TokenRequestDto createTokenRequestDto(String email, String password) {
        return new TokenRequestDto(email, password);
    }
}

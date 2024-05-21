package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.TokenRequest;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @DisplayName("사용자가 존재하지 않으면 예외가 발생한다.")
    @Test
    void createTokenThrowsExceptionWhenNotFoundMember() {
        TokenRequest tokenRequest = new TokenRequest("notFoundMember@wooteco.com", "password");

        assertThatCode(() -> authenticationService.createToken(tokenRequest))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_MEMBER);
    }

    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void createTokenThrowsExceptionWhenPasswordDoesNotMatch() {
        TokenRequest tokenRequest = new TokenRequest("admin@wooteco.com", "wrongPassword");

        assertThatCode(() -> authenticationService.createToken(tokenRequest))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}

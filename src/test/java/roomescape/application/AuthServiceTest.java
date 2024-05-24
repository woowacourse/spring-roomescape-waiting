package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.BasicAcceptanceTest;
import roomescape.dto.TokenRequest;
import roomescape.exception.RoomescapeException;

class AuthServiceTest extends BasicAcceptanceTest {
    @Autowired
    private AuthService authService;

    @DisplayName("존재하지 않는 회원일 때 예외를 발생시킨다.")
    @Test
    void name() {
        TokenRequest tokenRequest = new TokenRequest("gomding@wooteco.com", "dffd@efg32");

        assertThatThrownBy(() -> authService.createToken(tokenRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", tokenRequest.email()));
    }
}

package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"asdasdf.com", "asdasdf@.com", "@com", "@naver.com", "qwer.com@naver"})
    @DisplayName("예약 생성 시 이메일 형식이 아닐 경우, 예외를 반환한다.")
    void validateEmailInvalidType(String invalidEmail) {
        assertThatThrownBy(() -> new Member("몰리", Role.USER, invalidEmail, "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(invalidEmail + "은 이메일 형식이 아닙니다.");
    }
}

package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"asdasdf.com", "asdasdf@.com", "@com", "@naver.com", "qwer.com@naver"})
    @DisplayName("이메일 객체 생성 실패: 이메일 형식 오류 ")
    void validateEmailInvalidType(String invalidEmail) {
        assertThatThrownBy(() -> new Member("몰리", Role.USER, invalidEmail, "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(invalidEmail + "은 이메일 형식이 아닙니다.");
    }
}

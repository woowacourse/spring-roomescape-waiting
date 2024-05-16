package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@DisplayName("이메일 도메인 테스트")
class EmailTest {
    @DisplayName("형식에 맞지 않은 이메일 생성 시, 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "asdsad@", "@@", "@sadsad", "sadad@sadsad"})
    void email(String invalidEmail) {
        //given & when & then
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.EMAIL_FORMAT_ERROR.getMessage());
    }
}

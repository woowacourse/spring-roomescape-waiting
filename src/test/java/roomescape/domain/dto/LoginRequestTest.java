package roomescape.domain.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRequestTest {
    @DisplayName("필드 중 빈 값이 존재할 경우 예외를 발생한다.")
    @CsvSource({",password", "email,"})
    @ParameterizedTest
    void given_when_newWithEmptyValue_then_throwException(String email, String password) {
        assertThatThrownBy(() -> new LoginRequest(email, password))
                .isInstanceOf(EmptyValueNotAllowedException.class);
    }
}
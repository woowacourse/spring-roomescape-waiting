package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTest {

    @DisplayName("비밀번호는 8자 이상이다.")
    @ValueSource(strings = {"password", "rushrush1", "asdasdasd"})
    @ParameterizedTest
    void passwordLength(String password) {
        assertDoesNotThrow(() -> new Password(password));
    }

    @DisplayName("비밀번호는 8자 이상이 아닐 경우 예외처리.")
    @ValueSource(strings = {"", " ", "pasword"})
    @ParameterizedTest
    void invalidPasswordLength(String password) {
        Assertions.assertThatThrownBy(() -> new Password(password))
                .isInstanceOf(IllegalArgumentException.class);    }

}

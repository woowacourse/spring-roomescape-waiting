package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {


    @DisplayName("email 형식에 맞게 입력해야 한다.")
    @ValueSource(strings = {"asd@a.com", "a@a.com", "rush@woteco.com"})
    @ParameterizedTest
    void emailFormat(String email) {
        assertDoesNotThrow(() -> new Email(email));
    }


    @DisplayName("email 형식에 맞지 않을 시 예외처리.")
    @ValueSource(strings = {"", " ", "ab", "asd@asd"})
    @ParameterizedTest
    void invalidEmailFormat(String email) {
        Assertions.assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

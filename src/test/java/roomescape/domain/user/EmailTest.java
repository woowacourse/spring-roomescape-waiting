package roomescape.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidInputException;

class EmailTest {

    @ParameterizedTest
    @DisplayName("이메일 형식이 맞지 않으면 예외가 발생한다.")
    @ValueSource(strings = {"abc@email", "abc@email.", "abc@.com", "abc@.", "@email.com", "@email"})
    void emailFormatShouldValid(final String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail)).isInstanceOf(InvalidInputException.class);
    }
}

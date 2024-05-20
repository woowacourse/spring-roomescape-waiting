package roomescape.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @ParameterizedTest(name ="{0}은 적절한형식이 아닙니다." )
    @ValueSource(strings = {"i894","joyson5@.com","jerry.com"})
    @DisplayName("이메일 형식이 아니면 예외를 발생합니다.")
    void throw_exception_when_format_is_not_email(final String value) {
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

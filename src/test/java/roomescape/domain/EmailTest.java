package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

class EmailTest {

    @DisplayName("이메일 형식이 아닌 값으로 Email 객체를 생성할 수 없다.")
    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "wrongEmail.com",
            "wrongEmail@wrong.",
            "wrongEmail@.com"
    })
    void notEmailPatternExceptionTest(String wrongEmail) {
        assertThatThrownBy(() -> new Email(wrongEmail))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.INVALID_EMAIL.getMessage());
    }

    @DisplayName("올바른 이메일 형식으로 Email 객체를 생성할 수 있다.")
    @ParameterizedTest
    @ValueSource(strings = {
            "zangsu_@naver.com",
            "email@wooteco.net"
    })
    void collectEmailPatternTest(String wrongEmail) {
        assertThatCode(() -> new Email(wrongEmail))
                .doesNotThrowAnyException();
    }
}

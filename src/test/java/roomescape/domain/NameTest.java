package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static roomescape.exception.ExceptionType.INVALID_NAME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.RoomescapeException;

class NameTest {

    @DisplayName("잘못된 이름으로 Name 객체를 생성할 수 없다.")
    @ParameterizedTest
    @NullAndEmptySource
    void wrongNameTest(String name) {
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(INVALID_NAME.getMessage());
    }

    @DisplayName("정상적인 이름으로 Name 객체를 생성할 수 있다.")
    @Test
    void collectNameTest() {
        assertThatCode(() -> new Name("제이미"))
                .doesNotThrowAnyException();
    }
}

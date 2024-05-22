package roomescape.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MEMBER_CAT_NAME;

class NameTest {

    @Test
    @DisplayName("예약자 이름을 생성한다.")
    void createName() {
        assertThatCode(() -> new Name(MEMBER_CAT_NAME))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"123", "", " "})
    @DisplayName("예약자 이름이 빈칸이거나 숫자로만 구성될 경우 예외를 발생한다.")
    void throwExceptionWhenInvalidName(final String invalidName) {
        assertThatThrownBy(() -> new Name(invalidName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"miaIsLovely"})
    @DisplayName("사용자 이름은 10자 이하이다.")
    void validateLength(String invalidName) {
        // when & then
        assertThatThrownBy(() -> new Name(invalidName))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("사용자 이름은 숫자로만 구성될 수 없다.")
    void validatePattern() {
        // given
        String invalidName = "123456";

        // when & then
        assertThatThrownBy(() -> new Name(invalidName))
                .isInstanceOf(ViolationException.class);

    }
}

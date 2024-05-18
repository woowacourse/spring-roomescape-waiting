package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class MemberPasswordTest {

    @DisplayName("올바른 비밀 번호 형식이면 생성된다.")
    @Test
    void create() {
        assertThatCode(() -> new MemberPassword("abc1234"))
            .doesNotThrowAnyException();
    }

    @DisplayName("비밀 번호가 빈 값이면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void create_Fail(String input) {
        assertThatThrownBy(() -> new MemberPassword(input))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

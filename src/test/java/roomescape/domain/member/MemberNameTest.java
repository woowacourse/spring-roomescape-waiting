package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @ParameterizedTest
    @DisplayName("이름이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateName(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 필수 값입니다.");
    }

    @Test
    @DisplayName("이름이 30자를 넘으면 예외가 발생한다.")
    void validateNameLength() {
        String name = "a".repeat(31);

        assertThatThrownBy(() -> new MemberName(name))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 30자를 넘을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자명에 영어 한글 외의 문자가 포함되어있으면 예외가 발생한다.")
    void validateNamePattern() {
        assertThatThrownBy(() -> new MemberName("홍길동1"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자명은 영어, 한글만 가능합니다.");
    }
}

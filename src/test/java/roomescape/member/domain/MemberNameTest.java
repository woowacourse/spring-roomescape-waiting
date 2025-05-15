package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @DisplayName("사용자 이름 값을 통해 사용자 이름을 생성할 수 있다.")
    @ValueSource(strings = {"12", "1234567890"})
    @ParameterizedTest
    void createMemberNameByValidValue(String value) {
        assertThatCode(() -> new MemberName(value))
                .doesNotThrowAnyException();
    }

    @DisplayName("사용자 이름 값이 공백이면 사용자 이름을 생성할 수 없다.")
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    @ParameterizedTest
    void createMemberNameByBlankValue(String value) {
        assertThatThrownBy(() -> new MemberName(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("사용자 이름 값이 유효 길이 범위를 벗어나면 사용자 이름을 생성할 수 없다.")
    @ValueSource(strings = {"1", "a1234567890"})
    @ParameterizedTest
    void createMemberNameByInvalidLength(String value) {
        assertThatThrownBy(() -> new MemberName(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

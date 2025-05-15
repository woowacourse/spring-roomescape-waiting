package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"aaaaaaaaaaaaaaa"})
    @DisplayName("맴버 이름 예외 테스트")
    void name_exception_test(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
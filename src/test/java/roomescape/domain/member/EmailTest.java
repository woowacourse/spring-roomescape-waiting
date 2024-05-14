package roomescape.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidMemberException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EmailTest {
    @DisplayName("잘못된 이메일 형식으로 생성하면 예외를 발생시킨다.")
    @EmptySource
    @ParameterizedTest
    @ValueSource(strings = {"lini@", "lini.com"})
    void invalidEmailFormat(String email) {
        assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessage("유효하지 않은 이메일입니다.");
    }
}

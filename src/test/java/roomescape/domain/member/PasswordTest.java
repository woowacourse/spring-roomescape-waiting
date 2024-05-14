package roomescape.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidMemberException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PasswordTest {
    @DisplayName("올바르지 않은 비밀번호 양식이면 예외를 던진다.")
    @EmptySource
    @ParameterizedTest
    @ValueSource(strings = {"linin", "linirinilinir"})
    void invalidPassword(String password) {
        assertThatThrownBy(() -> new Password(password))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessage("비밀번호는 6자 이상 12자 이하여야 합니다.");
    }

}

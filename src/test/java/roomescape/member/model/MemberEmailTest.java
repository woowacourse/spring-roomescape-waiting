package roomescape.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberEmailTest {

    @DisplayName("null 혹은 공백의 값이 입력되면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void memberEmailLengthTest(final String invalidName) {
        // When & Then
        assertThatThrownBy(() -> new MemberEmail(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 값은 공백일 수 없습니다.");
    }

    @DisplayName("유효하지 않은 형식의 이메일이 입력되면 예외를 발생시킨다.")
    @ValueSource(strings = {"kelly6bf", "kelly6bf#gmail.com", "kelly6bf @ gmail . com"})
    @ParameterizedTest
    void validateInvalidRegexTest(final String invalidName) {
        // When & Then
        assertThatThrownBy(() -> new MemberEmail(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 이메일 형식입니다.");
    }
}

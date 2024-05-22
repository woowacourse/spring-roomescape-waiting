package roomescape.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberNameTest {

    @DisplayName("공백의 회원 이름이 입력되면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void validateNullOrEmptyTest(final String invalidName) {
        // When & Then
        assertThatThrownBy(() -> new MemberName(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 이름으로 공백을 입력할 수 없습니다.");
    }

    @DisplayName("유효하지 않은 길이의 이름이 입력되면 예외를 발생시킨다.")
    @Test
    void validateNameLengthTest() {
        // Given
        final String invalidNameInput = "kelly6bff";

        // When & Then
        assertThatThrownBy(() -> new MemberName(invalidNameInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 이름은 8글자 이하여만 합니다.");
    }
}

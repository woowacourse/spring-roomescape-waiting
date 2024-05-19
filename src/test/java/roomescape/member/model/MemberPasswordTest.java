package roomescape.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberPasswordTest {

    @DisplayName("공백 혹은 null이 입력되면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void validateMemberPasswordTest(final String invalidName) {
        // When & Then
        assertThatThrownBy(() -> new MemberPassword(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 비밀번호로 공백을 입력할 수 없습니다.");
    }
}

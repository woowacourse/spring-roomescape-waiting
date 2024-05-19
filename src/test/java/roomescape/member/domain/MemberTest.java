package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {
    @DisplayName("이름이 비어있을 때 예외를 던진다.")
    @Test
    void validateThemeTest_whenNameIsNull() {
        assertThatThrownBy(() -> new Member(1L, null, "abc@abc.com"))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("이메일이 비어있을 때 예외를 던진다.")
    @Test
    void validateThemeTest_whenEmailIsNull() {
        assertThatThrownBy(() -> new Member(1L, "커찬", null))
                .isInstanceOf(NullPointerException.class);
    }
}

package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("회원을 생성한다.")
    void create() {
        assertThatCode(() -> new Member("example@gmail.com", "abc123", "구름", Role.USER))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("역할이 없으면 예외가 발생한다.")
    void validateRole() {
        assertThatThrownBy(() -> new Member("example@gmail.com", "abc123", "구름", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("역할은 필수 값입니다.");
    }
}

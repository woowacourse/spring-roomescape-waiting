package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, VALID_USER_PASSWORD,
            MemberRole.USER))
            .doesNotThrowAnyException();
    }

    @DisplayName("이름이 빈 값이면 예외가 발생한다.")
    @Test
    void create_ByBlankName() {
        assertThatThrownBy(
            () -> new Member(1L, null, VALID_USER_EMAIL, VALID_USER_PASSWORD, MemberRole.USER))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("이메일 또는 비밀 번호가 null이면 예외가 발생한다.")
    @Test
    void create_ByNullEmailOrPassword() {
        assertThatThrownBy(() -> new Member(1L, VALID_USER_NAME, null, VALID_USER_PASSWORD, MemberRole.USER))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Member(1L, VALID_USER_NAME, VALID_USER_EMAIL, null, MemberRole.USER))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

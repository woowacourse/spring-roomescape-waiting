package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class MemberTest {

    @DisplayName("회원 정보를 저장한다.")
    @Test
    void create() {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getLoginId()).isEqualTo("roro");
        assertThat(member.getName()).isEqualTo("러로");
        assertThat(member.getPassword()).isEqualTo("password");
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @DisplayName("ID가 null이어도 아직 저장 전 도메인으로 생성할 수 있다.")
    @Test
    void nullId() {
        Member member = new Member(null, "roro", "러로", "password", Role.USER);

        assertThat(member.getId()).isNull();
    }

    @DisplayName("로그인 ID, 이름, 비밀번호는 null, 빈 문자열, 공백일 수 없다.")
    @Test
    void requiredFields() {
        assertInvalidInput(() -> new Member(1L, null, "러로", "password", Role.USER));
        assertInvalidInput(() -> new Member(1L, "", "러로", "password", Role.USER));
        assertInvalidInput(() -> new Member(1L, "   ", "러로", "password", Role.USER));

        assertInvalidInput(() -> new Member(1L, "roro", null, "password", Role.USER));
        assertInvalidInput(() -> new Member(1L, "roro", "", "password", Role.USER));
        assertInvalidInput(() -> new Member(1L, "roro", "   ", "password", Role.USER));

        assertInvalidInput(() -> new Member(1L, "roro", "러로", null, Role.USER));
        assertInvalidInput(() -> new Member(1L, "roro", "러로", "", Role.USER));
        assertInvalidInput(() -> new Member(1L, "roro", "러로", "   ", Role.USER));
    }

    @DisplayName("역할은 null일 수 없다.")
    @Test
    void requiredRole() {
        assertInvalidInput(() -> new Member(1L, "roro", "러로", "password", null));
    }

    @DisplayName("이름은 10자를 초과할 수 없다.")
    @Test
    void nameMaxLength() {
        new Member(1L, "roro", "가".repeat(10), "password", Role.USER);

        assertInvalidInput(() -> new Member(1L, "roro", "가".repeat(11), "password", Role.USER));
    }

    private void assertInvalidInput(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.INVALID_INPUT);
    }
}

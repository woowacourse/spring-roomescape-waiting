package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("회원을 생성하면 기본 권한은 USER이다")
    void 회원_생성_시_기본_권한은_USER() {
        Member member = Member.of("현미밥", "test@test.com", "1234");

        assertThat(member.getName()).isEqualTo("현미밥");
        assertThat(member.getEmail()).isEqualTo("test@test.com");
        assertThat(member.getPassword()).isEqualTo("1234");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("ADMIN 권한으로 복원하면 isAdmin이 true이다")
    void ADMIN_권한_복원_시_isAdmin_true() {
        Member member = Member.restore(1L, "관리자", "admin@test.com", "1234", Role.ADMIN);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getRole()).isEqualTo(Role.ADMIN);
        assertThat(member.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("이름이 비어 있으면 예외가 발생한다")
    void 이름_없으면_예외() {
        assertThatThrownBy(() -> Member.of(" ", "test@test.com", "1234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이메일이 비어 있으면 예외가 발생한다")
    void 이메일_없으면_예외() {
        assertThatThrownBy(() -> Member.of("현미밥", null, "1234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 예외가 발생한다")
    void 비밀번호_없으면_예외() {
        assertThatThrownBy(() -> Member.of("현미밥", "test@test.com", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("권한이 null이면 예외가 발생한다")
    void 권한_null_예외() {
        assertThatThrownBy(() -> Member.restore(1L, "현미밥", "test@test.com", "1234", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

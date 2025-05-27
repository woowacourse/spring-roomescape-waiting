package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("사용자의 비밀번호가 일치하면 true를 반환한다")
    @Test
    void matchPassword() {
        // given
        String password = "password";
        Member member = Member.withoutId("user", "user1@email.com", password, Role.USER);

        // when
        boolean matchPassword = member.matchPassword(password);

        // then
        assertThat(matchPassword).isTrue();
    }

    @DisplayName("사용자의 비밀번호가 일치하지 않으면 false를 반환한다")
    @Test
    void missMatchPassword() {
        // given
        String password = "password";
        Member member = Member.withoutId("user", "user1@email.com", password, Role.USER);

        // when
        boolean matchPassword = member.matchPassword("otherPassword");

        // then
        assertThat(matchPassword).isFalse();
    }

    @DisplayName("관리자라면 true를 반환한다")
    @Test
    void isAdmin() {
        // given
        Member member = Member.withoutId("admin", "admin1@email.com", "password", Role.ADMIN);

        // when
        boolean isAdmin = member.isAdmin();

        // then
        assertThat(isAdmin).isTrue();
    }

    @DisplayName("관리자가 아니라면 false를 반환한다")
    @Test
    void isNotAdmin() {
        // given
        Member member = Member.withoutId("user", "user1@email.com", "password", Role.USER);

        // when
        boolean isAdmin = member.isAdmin();

        // then
        assertThat(isAdmin).isFalse();
    }
}
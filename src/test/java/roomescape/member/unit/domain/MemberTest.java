package roomescape.member.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

class MemberTest {

    @Test
    @DisplayName("비밀번호가 일치하면 true를 반환한다.")
    void validatePassword() {
        // given
        Member member = new Member(1L, "미소", "miso@email.com", "password", RoleType.USER);

        // when
        boolean result = member.matchesPassword("password");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 false를 반환한다.")
    void validateWrongPassword() {
        // given
        Member member = new Member(1L, "미소", "miso@email.com", "password", RoleType.USER);

        // when
        boolean result = member.matchesPassword("wrongPassword");

        // then
        assertThat(result).isFalse();
    }
} 
package roomescape.unit.domain.reservation;

import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MemberTest {

    @Test
    void 같은_username을_가지고_있는지_확인할_수_있다() {
        // Given
        Member member = new Member(null, "user@user.com", "password", "user", Role.USER);

        // When & Then
        assertThat(member.isSameUsername("user@user.com")).isTrue();
    }

    @Test
    void 다른_username으로_비교하는_경우에는_다른_username이라는_응답을_받아야_한다() {
        // Given
        Member member = new Member(null, "user@user.com", "password", "user", Role.USER);

        // When & Then
        assertThat(member.isSameUsername("invalid@user.com")).isFalse();
    }
}

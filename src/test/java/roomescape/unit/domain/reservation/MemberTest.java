package roomescape.unit.domain.reservation;

import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MemberTest {

    @Test
    void id값을_가진_엔티티로_변환할_수_있다() {
        // Given
        Member member = new Member(null, "user@user.com", "password", "user", Role.USER);

        // When
        Member memberWithId = member.toEntity(1);

        // Then
        assertAll(() -> {
            assertThat(memberWithId.getId()).isEqualTo(1);
            assertThat(memberWithId.getUsername()).isEqualTo("user@user.com");
            assertThat(memberWithId.getPassword()).isEqualTo("password");
            assertThat(memberWithId.getName()).isEqualTo("user");
            assertThat(memberWithId.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    void 같은_username을_가지고_있는지_확인할_수_있다() {
        // Given
        Member member = new Member(null, "user@user.com", "password", "user", Role.USER);

        // When & Then
        assertAll(() -> {
            assertThat(member.isSameUsername("user@user.com")).isTrue();
            assertThat(member.isSameUsername("user2@user.com")).isFalse();
        });
    }
}

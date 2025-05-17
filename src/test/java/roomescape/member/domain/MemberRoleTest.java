package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MemberRoleTest {

    @Test
    void ADMIN인_경우_isAdmin_은_true를_반환() {
        assertThat(MemberRole.ADMIN.isAdmin()).isTrue();
    }

    @Test
    void MEMBER인_경우_isAdmin은_false를_반환한다() {
        assertThat(MemberRole.MEMBER.isAdmin()).isFalse();
    }
}

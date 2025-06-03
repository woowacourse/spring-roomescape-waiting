package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import roomescape.domain.member.MemberRole;

class MemberRoleTest {

    @Test
    void 권한이름으로_역할을_반환한다() {
        // when // then
        assertThat(MemberRole.from("ADMIN")).isEqualTo(MemberRole.ADMIN);
        assertThat(MemberRole.from("MEMBER")).isEqualTo(MemberRole.MEMBER);
    }
} 

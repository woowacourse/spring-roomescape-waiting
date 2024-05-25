package roomescape.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.utils.TestFixture;

class MemberTest {
    @Test
    @DisplayName("회원이 관리자인지 확인할 수 있다.")
    void isAdmin() {
        final Member member = TestFixture.getMember();

        assertThat(member.isNotAdmin()).isTrue();
    }
}

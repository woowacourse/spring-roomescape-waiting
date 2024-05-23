package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialMemberFixture.ADMIN;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialWaitingFixture.WAITING_1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {

    @Test
    @DisplayName("예약 대기를 건 회원은 예약 대기를 삭제할 권한이 있다.")
    void MemberWhoWaitsHasDeleteAuth() {
        boolean hasDeleteAuth = WAITING_1.hasDeleteAuth(MEMBER_2);

        assertThat(hasDeleteAuth).isTrue();
    }

    @Test
    @DisplayName("관리자는 모든 예약 대기를 삭제할 권한이 있다.")
    void AdminHasDeleteAuth() {
        boolean hasDeleteAuth = WAITING_1.hasDeleteAuth(ADMIN);

        assertThat(hasDeleteAuth).isTrue();
    }

    @Test
    @DisplayName("관련 없는 회원은 예약 대기를 삭제할 권한이 없다.")
    void NotRelatedMemberCanNotDeleteWaiting() {
        boolean hasDeleteAuth = WAITING_1.hasDeleteAuth(MEMBER_1);

        assertThat(hasDeleteAuth).isFalse();
    }
}

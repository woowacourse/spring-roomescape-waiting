package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialMemberFixture.ADMIN;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialReservationFixture.RESERVATION_1;
import static roomescape.InitialReservationFixture.RESERVATION_2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("Reservation 객체의 동등성을 따질 때는 id만 확인한다.")
    void testEquals() {
        Reservation reservation = new Reservation(
                RESERVATION_1.getId(),
                RESERVATION_2.getDate(),
                RESERVATION_2.getReservationTime(),
                RESERVATION_2.getTheme(),
                MEMBER_1
        );

        assertThat(RESERVATION_1).isEqualTo(reservation);
    }

    @Test
    @DisplayName("예약을 건 회원은 예약을 삭제할 권한이 있다.")
    void MemberWhoReservatedHasDeleteAuth() {
        boolean hasDeleteAuth = RESERVATION_1.hasNoAuthToDeleteThis(MEMBER_1);

        assertThat(hasDeleteAuth).isFalse();
    }

    @Test
    @DisplayName("관리자는 모든 예약에 대해 삭제할 권한이 있다.")
    void AdminHasDeleteAuth() {
        boolean hasDeleteAuth = RESERVATION_1.hasNoAuthToDeleteThis(ADMIN);

        assertThat(hasDeleteAuth).isFalse();
    }

    @Test
    @DisplayName("관련 없는 회원은 예약을 삭제할 권한이 없다.")
    void NotRelatedMemberCanNotDeleteReservation() {
        boolean hasDeleteAuth = RESERVATION_1.hasNoAuthToDeleteThis(MEMBER_2);

        assertThat(hasDeleteAuth).isTrue();
    }
}

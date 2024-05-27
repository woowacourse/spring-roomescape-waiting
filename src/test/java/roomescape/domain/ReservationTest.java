package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    @DisplayName("자신의 예약인지 확인한다.")
    void isOwn() {
        final Member member = new Member(1L, "레디", "redddy@gmail.com", "password", Role.ADMIN);
        final Member anotherMember = new Member(2L, "재즈", "gkatjraud@redddybabo.com", "password", Role.USER);
        final Reservation reservation = new Reservation(null, member, null, null, null);

        assertThat(reservation.isOwn(member.getId())).isTrue();
        assertThat(reservation.isOwn(anotherMember.getId())).isFalse();
    }
}

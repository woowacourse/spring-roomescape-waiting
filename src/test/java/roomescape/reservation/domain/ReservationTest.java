package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class ReservationTest {

    @Test
    @DisplayName("예약 생성 시 기본으로 예약 상태 값을 가진다")
    void shouldHaveReservedStatusWhenCreated() {
        final Member member = new Member("admin@email.com", "admin", "어드민", Role.ADMIN);
        final Theme theme = new Theme("theme1", "description", "thumbnail");
        final ReservationTime reservationTime = new ReservationTime(LocalTime.MIDNIGHT);

        Reservation reservation = Reservation.createReserved(member, theme, LocalDate.now(), reservationTime);

        assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED);
    }
}

package roomescape.reservation.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class MyReservationResponseTest {

    @Test
    @DisplayName("예악 상태를 정확히 출력한다.")
    void status_test_when_reserved() {
        // given
        Member member = Member.createWithId(1L, "a", "a@naver.com", "1234", Role.USER);
        Theme theme = Theme.createWithId(1L, "b", "b", "b");
        ReservationTime time = ReservationTime.createWithId(1L, LocalTime.of(10, 0));
        Reservation reservation = Reservation.createWithId(1L, member, LocalDate.of(2025, 5, 24),
                time, theme, ReservationStatus.RESERVED,
                LocalDateTime.now());
        ReservationWithRank reservationWithRank = new ReservationWithRank(reservation, 0L);
        MyReservationResponse expected = new MyReservationResponse(1L, "b", LocalDate.of(2025, 5, 24),
                LocalTime.of(10, 0), "예약");
        // when
        MyReservationResponse response = MyReservationResponse.from(reservationWithRank);
        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("예악 대기 상태를 정확히 출력한다.")
    void status_test_when_waiting() {
        // given
        Member member = Member.createWithId(1L, "a", "a@naver.com", "1234", Role.USER);
        Theme theme = Theme.createWithId(1L, "b", "b", "b");
        ReservationTime time = ReservationTime.createWithId(1L, LocalTime.of(10, 0));
        Reservation reservation = Reservation.createWithId(1L, member, LocalDate.of(2025, 5, 24),
                time, theme, ReservationStatus.WAITED,
                LocalDateTime.now());
        ReservationWithRank reservationWithRank = new ReservationWithRank(reservation, 3L);
        MyReservationResponse expected = new MyReservationResponse(1L, "b", LocalDate.of(2025, 5, 24),
                LocalTime.of(10, 0), "3번째 예약대기");
        // when
        MyReservationResponse response = MyReservationResponse.from(reservationWithRank);
        // then
        assertThat(response).isEqualTo(expected);
    }
}
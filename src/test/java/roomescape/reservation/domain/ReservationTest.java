package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
class ReservationTest {

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("Reserved 상태인 예약을 대기 상태로 변경하면 waiting 상태로 변한다.")
    void pendingTest() {
        Theme theme = Theme.create("판타지", "https://example.com/theme.png", "설명");
        ReservationTime time = ReservationTime.create(LocalDateTime.now(clock).plusHours(1).toLocalTime());

        Reservation reservation = Reservation.create(
                "포비",
                LocalDate.now(clock).plusDays(1),
                time,
                theme,
                Status.RESERVED,
                clock
        );

        Reservation pendingReservation = reservation.modify(
                LocalDate.now(clock).plusDays(2),
                time,
                theme,
                Status.WAITING,
                clock
        );

        Assertions.assertThat(pendingReservation.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("대기 상태인 예약을 취소한다.")
    void cancelTest() {
        ReservationTime time = ReservationTime.restore(1L, LocalDateTime.now(clock).plusHours(1).toLocalTime(), true);

        Theme theme = Theme.restore(1L, "판타지", "https://example.com/theme.png", "설명", true);

        Reservation reservation = Reservation.restore(
                1L, "포비", LocalDate.now(clock).plusDays(1), time, theme, Status.WAITING, LocalDateTime.now(clock)
        );

        Reservation canceledReservation = reservation.cancel();
        Assertions.assertThat(canceledReservation.getStatus()).isEqualTo(Status.CANCELED);
    }

}

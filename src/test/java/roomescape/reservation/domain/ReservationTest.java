package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
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
    @DisplayName("Active 상태인 예약을 대기 상태로 변경하면 pending 상태로 변한다.")
    void pendingTest() {
        Theme theme = Theme.builder()
                .name("판타지")
                .thumbnailImageUrl("https://~~~~")
                .description("설명")
                .durationTime(LocalTime.now(clock))
                .build();
        ReservationTime time = ReservationTime.builder()
                .startAt(LocalTime.now(clock).plusHours(1))
                .build();

        Reservation reservation = Reservation.builder()
                .name("포비")
                .date(LocalDate.now().plusDays(1))
                .theme(theme)
                .status(Status.ACTIVE).time(time)
                .build();

        Reservation pendingReservation = reservation.pending(
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                theme,
                clock
        );
        Assertions.assertThat(pendingReservation.getStatus()).isEqualTo(Status.PENDING);
    }
}

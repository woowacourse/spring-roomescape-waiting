package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyReservationTest {

    private static final ReservationTime TIME = new ReservationTime(1L,
            LocalDateTime.of(2030, 6, 1, 10, 0),
            LocalDateTime.of(2030, 6, 1, 12, 0));

    private static final Theme THEME = new Theme("테마", "설명", "test-url").withId(1L);

    @DisplayName("RESERVED 상태의 예약은 순번 0이다")
    @Test
    void RESERVED는_순번_0_테스트() {
        Reservation reserved = new Reservation("라이", TIME, THEME, Status.RESERVED, LocalDateTime.now()).withId(1L);
        ReservationWaitings waitings = new ReservationWaitings(List.of());

        MyReservation result = MyReservation.of(reserved, waitings);

        assertThat(result.waitingOrder()).isEqualTo(0);
    }

    @DisplayName("WAITING 상태의 예약은 슬롯 순번을 반환한다")
    @Test
    void WAITING은_슬롯_순번_테스트() {
        Reservation older = new Reservation("어셔", TIME, THEME, Status.WAITING,
                LocalDateTime.of(2030, 5, 1, 10, 0)).withId(1L);
        Reservation mine = new Reservation("라이", TIME, THEME, Status.WAITING,
                LocalDateTime.of(2030, 5, 2, 10, 0)).withId(2L);
        ReservationWaitings waitings = new ReservationWaitings(List.of(older, mine));

        MyReservation result = MyReservation.of(mine, waitings);

        assertThat(result.waitingOrder()).isEqualTo(2);
    }
}

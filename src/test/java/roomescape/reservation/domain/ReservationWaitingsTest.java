package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationWaitingsTest {

    @DisplayName("earliest는 가장 오래된 WAITING을 반환한다")
    @Test
    void earliest_가장_오래된_반환() {
        // given
        Reservation newest = waitingAt(3L, LocalDateTime.of(2030, 5, 3, 10, 0));
        Reservation oldest = waitingAt(1L, LocalDateTime.of(2030, 5, 1, 10, 0));
        Reservation middle = waitingAt(2L, LocalDateTime.of(2030, 5, 2, 10, 0));

        // when
        ReservationWaitings waitings = new ReservationWaitings(List.of(newest, oldest, middle));

        // then
        assertThat(waitings.earliest()).hasValue(oldest);
    }

    @DisplayName("order는 WAITING의 created_at 기준 순번을 반환한다")
    @Test
    void order_순번_반환테스트() {
        Reservation r1 = waitingAt(1L, LocalDateTime.of(2030, 5, 1, 10, 0));
        Reservation r2 = waitingAt(2L, LocalDateTime.of(2030, 5, 2, 10, 0));
        Reservation r3 = waitingAt(3L, LocalDateTime.of(2030, 5, 3, 10, 0));

        ReservationWaitings waitings = new ReservationWaitings(List.of(r3, r1, r2));

        assertThat(waitings.order(r1.getId())).isEqualTo(1);
        assertThat(waitings.order(r2.getId())).isEqualTo(2);
        assertThat(waitings.order(r3.getId())).isEqualTo(3);
    }

    @DisplayName("컬렉션에 없는 id에 대한 order는 0을 반환한다")
    @Test
    void order_없는_id는_0() {
        Reservation reservation = waitingAt(1L, LocalDateTime.of(2030, 5, 1, 10, 0));
        ReservationWaitings waitings = new ReservationWaitings(List.of(reservation));

        assertThat(waitings.order(999L)).isEqualTo(0);
    }

    @DisplayName("WAITING이 아닌 예약이 섞여있으면 생성 시 예외가 발생한다")
    @Test
    void 생성자_WAITING_아닌_예약_있으면_예외() {
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "test-url").withId(1L);
        Reservation reserved = new Reservation("이름", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);

        assertThatThrownBy(() -> new ReservationWaitings(List.of(reserved)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Reservation waitingAt(long id, LocalDateTime createdAt) {
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "test-url").withId(1L);
        return new Reservation("이름", time, theme, Status.WAITING, createdAt).withId(id);
    }
}

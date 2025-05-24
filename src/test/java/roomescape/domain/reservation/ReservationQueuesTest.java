package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixtures.anyUserWithNewId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

public class ReservationQueuesTest {

    private static final AtomicLong DUMMY_ID_GENERATOR = new AtomicLong();

    private static final User user = anyUserWithNewId();
    private static final LocalDate date = LocalDate.of(2025, 5, 1);
    private static final Theme theme = TestFixtures.anyThemeWithNewId();
    private static final TimeSlot time1 = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final TimeSlot time2 = new TimeSlot(2L, LocalTime.of(11, 0));

    @Test
    @DisplayName("대기열의 예약들과 비교해 주어진 예약의 대기 순번을 계산한다.")
    void orderOf() {
        // given
        var first = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var second = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var third = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var fourth = reservationOf(theme, date, time1, ReservationStatus.WAITING);

        var queues = new ReservationQueues(List.of(first, second, third, fourth));

        // when
        var order = queues.orderOf(fourth);

        // then
        assertThat(order).isEqualTo(4);
    }

    @Test
    @DisplayName("대기열의 예약들과 비교해 주어진 모든 예약의 대기 순번을 계산한다.")
    void orderOfAll() {
        // given
        var slot1_first = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var slot1_second = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var slot1_third = reservationOf(theme, date, time1, ReservationStatus.WAITING);

        var slot2_first = reservationOf(theme, date, time2, ReservationStatus.WAITING);
        var slot2_second = reservationOf(theme, date, time2, ReservationStatus.WAITING);

        var queues = new ReservationQueues(List.of(slot1_first, slot1_second, slot1_third, slot2_first, slot2_second));

        // when
        var reservationsToOrder = List.of(slot1_third, slot2_second);
        var reservationWithOrders = queues.orderOfAll(reservationsToOrder);

        // then
        assertThat(reservationWithOrders).contains(
            new ReservationWithOrder(slot1_third, 3),
            new ReservationWithOrder(slot2_second, 2)
        );
    }

    @Test
    @DisplayName("주어진 예약 다음 순번의 예약을 대기열에서 찾는다.")
    void findNext() {
        // given
        var first = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var second = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var third = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var fourth = reservationOf(theme, date, time1, ReservationStatus.WAITING);

        var queues = new ReservationQueues(List.of(first, second, third, fourth));

        // when & then
        assertAll(
            () -> assertThat(queues.findNext(first)).hasValue(second),
            () -> assertThat(queues.findNext(second)).hasValue(third),
            () -> assertThat(queues.findNext(third)).hasValue(fourth),
            () -> assertThat(queues.findNext(fourth)).isEmpty()
        );
    }

    private Reservation reservationOf(final Theme theme, final LocalDate date, final TimeSlot timeSlot, final ReservationStatus status) {
        return new Reservation(
            DUMMY_ID_GENERATOR.incrementAndGet(),
            user,
            ReservationSlot.of(date, timeSlot, theme),
            status
        );
    }
}

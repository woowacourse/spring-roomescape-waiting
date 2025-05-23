package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixtures.anyUserWithNewId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

public class QueuesTest {

    private static final AtomicLong DUMMY_ID_GENERATOR = new AtomicLong();

    private static final User user = anyUserWithNewId();
    private static final LocalDate date = LocalDate.of(2025, 5, 1);
    private static final Theme theme = TestFixtures.anyThemeWithNewId();
    private static final TimeSlot time1 = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final TimeSlot time2 = new TimeSlot(2L, LocalTime.of(11, 0));

    @Test
    @DisplayName("가지고 있는 예약들과 비교해 주어진 예약의 대기 순번을 계산한다.")
    void orderOf() {
        // given
        var reservations = new ArrayList<Reservation>();
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));

        var reservation = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        reservations.add(reservation);

        var queues = new Queues(reservations);


        // when
        var order = queues.orderOf(reservation);

        // then
        assertThat(order).isEqualTo(4);
    }

    @Test
    @DisplayName("가지고 있는 예약들과 비교해 주어진 모든 예약의 대기 순번을 계산한다.")
    void orderOfAll() {
        // given
        var reservations = new ArrayList<Reservation>();
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));
        reservations.add(reservationOf(theme, date, time1, ReservationStatus.WAITING));

        var reservation1 = reservationOf(theme, date, time1, ReservationStatus.WAITING);
        var reservation2 = reservationOf(theme, date, time2, ReservationStatus.RESERVED);
        reservations.add(reservation1);
        reservations.add(reservation2);

        var queues = new Queues(reservations);
        var reservationsToOrder = List.of(reservation1, reservation2);

        // when
        var reservationWithOrders = queues.orderOfAll(reservationsToOrder);

        // then
        assertThat(reservationWithOrders).contains(
            new ReservationWithOrder(reservation1, 4),
            new ReservationWithOrder(reservation2, 1)
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

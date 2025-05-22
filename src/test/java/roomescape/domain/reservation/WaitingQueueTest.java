package roomescape.domain.reservation;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DateUtils.today;
import static roomescape.DateUtils.tomorrow;
import static roomescape.TestFixtures.anyThemeWithNewId;
import static roomescape.TestFixtures.anyTimeSlotWithNewId;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.exception.AlreadyExistedException;

class WaitingQueueTest {

    private static final AtomicLong DUMMY_ID_GENERATOR = new AtomicLong();

    private final LocalDate tomorrow = tomorrow();
    private final TimeSlot timeSlot = anyTimeSlotWithNewId();
    private final Theme theme = anyThemeWithNewId();
    private final ReservationSlot slot = ReservationSlot.of(tomorrow, timeSlot, theme);
    private final ReservationSlot otherSlot = ReservationSlot.of(today(), timeSlot, theme);

    private final User user1 = TestFixtures.anyUserWithNewId();
    private final User user2 = TestFixtures.anyUserWithNewId();
    private final User user3 = TestFixtures.anyUserWithNewId();

    @Test
    @DisplayName("대기열에 예약을 추가한다.")
    void join() {
        // given
        var reservation1 = reservationOf(slot, user1);
        var queue = new WaitingQueue(slot, List.of(reservation1));

        var reservation2 = reservationOf(slot, user2);

        // when
        var order = queue.join(reservation2);

        // then
        assertThat(order).isEqualTo(2);
    }

    @Test
    @DisplayName("대기열에 예약을 추가할 때 중복된 예약이면 예외가 발생한다.")
    void joinDuplicated() {
        // given
        var reservation = reservationOf(slot, user1);
        var queue = new WaitingQueue(slot, List.of(reservation));

        // when & then
        assertThatThrownBy(() -> queue.join(reservation))
            .isInstanceOf(AlreadyExistedException.class);
    }

    @Test
    @DisplayName("대기열에 예약을 추가할 때 해당 예약의 슬롯이 다르면 예외가 발생한다.")
    void joinWithMismatchSlot() {
        // given
        var queue = new WaitingQueue(slot, emptyList());
        var reservation = reservationOf(otherSlot, user1);

        // when & then
        assertThatThrownBy(() -> queue.join(reservation))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("대기열이 비었는 지 확인한다.")
    void areWaitingsEmpty() {
        var queue = new WaitingQueue(slot, emptyList());
        assertThat(queue.areWaitingsEmpty()).isTrue();
    }

    @Test
    @DisplayName("주어진 예약의 대기 순번을 알 수 있다.")
    void orderOf() {
        // given
        var reservation1 = reservationOf(slot, user1);
        var reservation2 = reservationOf(slot, user2);
        var reservation3 = reservationOf(slot, user3);

        var queue = new WaitingQueue(slot, List.of(reservation1, reservation2, reservation3));

        // when & then
        assertAll(
            () -> assertThat(queue.orderOf(reservation1)).isEqualTo(1),
            () -> assertThat(queue.orderOf(reservation2)).isEqualTo(2),
            () -> assertThat(queue.orderOf(reservation3)).isEqualTo(3)
        );
    }

    @Test
    @DisplayName("대기열에 존재하지 않는 예약의 순번을 조회하려하면 예외가 발생한다.")
    void orderOfNotWaiting() {
        var queue = new WaitingQueue(slot, emptyList());
        var reservation = reservationOf(slot, user1);

        assertThatThrownBy(() -> queue.orderOf(reservation))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("다른 슬롯의 예약 순번을 조회하려하면 예외가 발생한다.")
    void orderOfMismatchSlot() {
        var queue = new WaitingQueue(slot, emptyList());
        var reservation = reservationOf(otherSlot, user1);

        assertThatThrownBy(() -> queue.orderOf(reservation))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private Reservation reservationOf(final ReservationSlot slot, final User user) {
        return new Reservation(
            DUMMY_ID_GENERATOR.incrementAndGet(),
            user,
            slot,
            ReservationStatus.RESERVED
        );
    }


}

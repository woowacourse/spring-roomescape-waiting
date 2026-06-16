package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.exception.AlreadyExistsException;

class ReservationsTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "테마", "설명", "/test");
    private final LocalDate date = LocalDate.now().plusDays(1);

    @Test
    void 중복_예약시_예외() {
        Reservation reservation = new Reservation(1L, "브라운", date, time, theme, ReservationStatus.CONFIRMED, null, null, null);
        Reservations reservations = new Reservations(List.of(reservation));

        assertThatThrownBy(() -> reservations.validateDuplicate("브라운"))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void 승인된_예약이_있으면_대기() {
        Reservation confirmed = new Reservation(1L, "브라운", date, time, theme, ReservationStatus.CONFIRMED, null, null, null);
        Reservations reservations = new Reservations(List.of(confirmed));

        assertThat(reservations.determineStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 승인된_예약이_없으면_승인() {
        Reservations reservations = new Reservations(List.of());

        assertThat(reservations.determineStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 다음_대기_조회() {
        Reservation waiting1 = new Reservation(2L, "아나키", date, time, theme, ReservationStatus.WAITING, null, null, null);
        Reservation waiting2 = new Reservation(3L, "그해", date, time, theme, ReservationStatus.WAITING, null, null, null);
        Reservations reservations = new Reservations(List.of(waiting1, waiting2));

        Optional<Reservation> next = reservations.findNextWaiting(1L);

        assertThat(next).isPresent();
        assertThat(next.get().getId()).isEqualTo(2L);
    }

    @Test
    void 대기_예약이_없으면_다음_대기_없음() {
        Reservation confirmed = new Reservation(1L, "브라운", date, time, theme, ReservationStatus.CONFIRMED, null, null, null);
        Reservations reservations = new Reservations(List.of(confirmed));

        Optional<Reservation> next = reservations.findNextWaiting(2L);

        assertThat(next).isEmpty();
    }
}

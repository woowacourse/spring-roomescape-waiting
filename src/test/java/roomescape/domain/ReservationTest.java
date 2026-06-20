package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.RoomEscapeException;

class ReservationTest {

    private static final String NAME = "와이제리";
    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final ReservationTime TIME = new ReservationTime(LocalTime.of(10, 0));
    private static final Theme THEME = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void 예약의_날짜와_시간이_현재보다_미래이면_False() {
        Reservation reservation = createReservation(FUTURE_SECOND_DATE, TIME);

        assertThat(reservation.isPast(NOW)).isFalse();
    }

    @Test
    void 예약의_날짜와_시간이_현재보다_과거이면_True() {
        LocalDate past = LocalDate.now().minusDays(1);

        Reservation reservation = createReservation(past, TIME);

        assertThat(reservation.isPast(NOW)).isTrue();
    }

    @Test
    void 오늘_날짜에_과거_시간이면_예약할_수_없다() {
        LocalDate now = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusMinutes(1);
        Reservation reservation = createReservation(now, new ReservationTime(pastTime));

        assertThatThrownBy(() -> reservation.verifyReservable(LocalDateTime.now()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 과거_날짜이면_예약할_수_없다() {
        LocalDate past = LocalDate.now().minusDays(1);
        Reservation reservation = createReservation(past, TIME);

        assertThatThrownBy(() -> reservation.verifyReservable(LocalDateTime.now()))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약자_이름이_일치하지_않으면_취소할_수_없다() {
        Reservation reservation = createReservation(FUTURE_SECOND_DATE, TIME);

        String other = "브라운";

        assertThatThrownBy(() -> reservation.verifyCancelableBy(other, NOW))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 이미_지난_예약은_취소할_수_없다() {
        LocalDate past = LocalDate.now().minusDays(1);

        Reservation reservation = createReservation(past, TIME);

        assertThatThrownBy(() -> reservation.verifyCancelableBy(NAME, NOW))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 변경할_수_있다() {
        Reservation reservation = createReservation(FUTURE_SECOND_DATE, TIME);
        LocalDate newDate = LocalDate.now().plusDays(1);
        ReservationTime newTime = new ReservationTime(LocalTime.of(11, 0));

        reservation = reservation.changeBy(NAME, NOW, newDate, newTime);

        assertThat(reservation.getDate()).isEqualTo(newDate);
        assertThat(reservation.getTime()).isEqualTo(newTime);
    }

    @Test
    void 예약자_이름이_일치하지_않으면_변경할_수_없다() {
        Reservation reservation = createReservation(FUTURE_SECOND_DATE, TIME);

        String other = "브라운";
        LocalDate newDate = LocalDate.now().plusDays(1);
        ReservationTime newTime = new ReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservation.changeBy(other, NOW, newDate, newTime))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 이미_지난_예약은_변경할_수_없다() {
        LocalDate past = LocalDate.now().minusDays(1);

        Reservation reservation = createReservation(past, TIME);
        LocalDate newDate = LocalDate.now().plusDays(1);
        ReservationTime newTime = new ReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservation.changeBy(NAME, NOW, newDate, newTime))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 과거_시점으로_변경할_수_없다() {
        Reservation reservation = createReservation(FUTURE_SECOND_DATE, TIME);
        LocalDate newDate = LocalDate.now().minusDays(1);
        ReservationTime newTime = new ReservationTime(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservation.changeBy(NAME, NOW, newDate, newTime))
            .isInstanceOf(RoomEscapeException.class);
    }

    private Reservation createReservation(LocalDate date, ReservationTime time) {
        return new Reservation(new Member(NAME), Slot.of(date, time, THEME));
    }

}

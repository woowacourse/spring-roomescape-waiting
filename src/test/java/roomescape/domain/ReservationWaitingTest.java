package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidInputException;

public class ReservationWaitingTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");
    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);

    @Test
    void create로_생성된_예약_대기는_id와_sequence가_null이다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now());

        ReservationWaiting waiting = ReservationWaiting.create("브라운", reservation);

        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getSequence()).isNull();
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getReservation()).isEqualTo(reservation);
    }

    @Test
    void 과거_예약에_대기를_등록하면_예외가_발생한다() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Reservation expiredReservation = Reservation.restore(1L, "다른사람", yesterday, reservationTime, theme, LocalDateTime.now());

        assertThatThrownBy(() -> ReservationWaiting.create("브라운", expiredReservation))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 이미_예약한_이름으로_대기를_등록하면_예외가_발생한다() {
        Reservation reservation = Reservation.restore(1L, "브라운", tomorrow, reservationTime, theme, LocalDateTime.now());

        assertThatThrownBy(() -> ReservationWaiting.create("브라운", reservation))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void restore는_과거_날짜_시간이어도_예외_없이_복원된다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = Reservation.restore(1L, "다른사람", pastDate, reservationTime, theme, LocalDateTime.now());

        assertThatCode(() -> ReservationWaiting.restore(1L, "브라운", pastReservation, 1L, LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    void restore로_생성된_예약_대기의_필드가_올바르게_설정된다() {
        Reservation reservation = Reservation.restore(1L, "다른사람", tomorrow, reservationTime, theme, LocalDateTime.now());
        LocalDateTime createdAt = LocalDateTime.now();

        ReservationWaiting waiting = ReservationWaiting.restore(1L, "브라운", reservation, 2L, createdAt);

        assertThat(waiting.getId()).isEqualTo(1L);
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getReservation().getDate()).isEqualTo(tomorrow);
        assertThat(waiting.getReservation().getTime()).isEqualTo(reservationTime);
        assertThat(waiting.getReservation().getTheme()).isEqualTo(theme);
        assertThat(waiting.getSequence()).isEqualTo(2L);
        assertThat(waiting.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void promote는_대기자_이름으로_이전된_예약을_반환한다() {
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = Reservation.restore(1L, "브라운", futureDate, futureTime, theme, LocalDateTime.now());
        ReservationWaiting waiting = ReservationWaiting.restore(1L, "네오", reservation, 1L, LocalDateTime.now());

        Reservation promoted = waiting.promote();

        assertThat(promoted.getName()).isEqualTo("네오");
        assertThat(promoted.getDate()).isEqualTo(futureDate);
        assertThat(promoted.getTime()).isEqualTo(futureTime);
        assertThat(promoted.getTheme()).isEqualTo(theme);
        assertThat(promoted.getId()).isNull();
    }

    @Test
    void promote_호출_시_만료된_예약이면_예외가_발생한다() {
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        Reservation expiredReservation = Reservation.restore(1L, "브라운", LocalDate.now().minusDays(1), pastTime, theme, LocalDateTime.now());
        ReservationWaiting waiting = ReservationWaiting.restore(1L, "네오", expiredReservation, 1L, LocalDateTime.now());

        assertThatThrownBy(waiting::promote)
                .isInstanceOf(roomescape.exception.ExpiredDateTimeException.class);
    }
}

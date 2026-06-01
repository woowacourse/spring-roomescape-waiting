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
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;

public class ReservationWaitingTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");
    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);

    @Test
    void create로_생성된_예약_대기는_id와_sequence가_null이다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);

        ReservationWaiting waiting = ReservationWaiting.create("브라운", slot);

        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getSequence()).isNull();
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getSlot()).isEqualTo(slot);
    }

    @Test
    void 과거_슬롯에_대기를_등록하면_예외가_발생한다() {
        Slot pastSlot = Slot.restore(1L, LocalDate.now().minusDays(1), reservationTime, theme);

        assertThatThrownBy(() -> ReservationWaiting.create("브라운", pastSlot))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void restore는_과거_날짜_시간이어도_예외_없이_복원된다() {
        Slot pastSlot = Slot.restore(1L, LocalDate.now().minusDays(1), reservationTime, theme);

        assertThatCode(() -> ReservationWaiting.restore(1L, pastSlot, "브라운", 1L, LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    void restore로_생성된_예약_대기의_필드가_올바르게_설정된다() {
        Slot slot = Slot.restore(1L, tomorrow, reservationTime, theme);
        LocalDateTime createdAt = LocalDateTime.now();

        ReservationWaiting waiting = ReservationWaiting.restore(1L, slot, "브라운", 2L, createdAt);

        assertThat(waiting.getId()).isEqualTo(1L);
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(tomorrow);
        assertThat(waiting.getTime()).isEqualTo(reservationTime);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getSequence()).isEqualTo(2L);
        assertThat(waiting.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void promote는_대기자_이름으로_이전된_예약을_반환한다() {
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Slot slot = Slot.restore(1L, futureDate, futureTime, theme);
        ReservationWaiting waiting = ReservationWaiting.restore(1L, slot, "네오", 1L, LocalDateTime.now());

        Reservation promoted = waiting.promote();

        assertThat(promoted.getName()).isEqualTo("네오");
        assertThat(promoted.getDate()).isEqualTo(futureDate);
        assertThat(promoted.getTime()).isEqualTo(futureTime);
        assertThat(promoted.getTheme()).isEqualTo(theme);
        assertThat(promoted.getId()).isNull();
    }

    @Test
    void promote_호출_시_만료된_슬롯이면_예외가_발생한다() {
        Slot expiredSlot = Slot.restore(1L, LocalDate.now().minusDays(1), reservationTime, theme);
        ReservationWaiting waiting = ReservationWaiting.restore(1L, expiredSlot, "네오", 1L, LocalDateTime.now());

        assertThatThrownBy(waiting::promote)
                .isInstanceOf(ExpiredDateTimeException.class);
    }
}

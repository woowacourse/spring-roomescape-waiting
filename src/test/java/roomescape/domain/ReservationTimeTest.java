package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;

class ReservationTimeTest {

    @Test
    void startAt이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 예약_날짜와_시간이_현재보다_이전이면_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 11, 0);

        assertThat(time.isPast(date, now)).isTrue();
    }

    @Test
    void 예약_날짜와_시간이_현재보다_이후이면_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 9, 0);

        assertThat(time.isPast(date, now)).isFalse();
    }

    @Test
    void 예약_날짜와_시간이_현재와_정확히_같으면_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 6, 1);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        assertThat(time.isPast(date, now)).isFalse();
    }

    @Test
    void 해당_시간에_예약이_없으면_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(time.isNotReserved(List.of())).isTrue();
    }

    @Test
    void 해당_시간에_예약이_존재하면_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 1), time, theme);
        Reservation reservation = new Reservation(1L, new Member(1L, "브라운"), slot);

        assertThat(time.isNotReserved(List.of(reservation))).isFalse();
    }

    @Test
    void 다른_시간의_예약은_isNotReserved_계산에_포함되지_않는다() {
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "방탈출1", "설명", "https://thumb.com");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 1), time2, theme);
        Reservation reservation = new Reservation(1L, new Member(1L, "브라운"), slot);

        assertThat(time1.isNotReserved(List.of(reservation))).isTrue();
    }
}

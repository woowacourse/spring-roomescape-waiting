package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class ReservationTimeTest {

    @Test
    void 시작시간이_null이면_예약시간을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시작 시간은 비어 있을 수 없습니다.");
    }

    @Test
    void 어제_날짜는_과거이다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThat(time.isPastOn(yesterday)).isTrue();
    }

    @Test
    void 내일_날짜는_과거가_아니다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertThat(time.isPastOn(tomorrow)).isFalse();
    }

    @Test
    void 오늘이고_시작시간이_지금보다_이전이면_과거이다() {
        LocalTime oneHourAgo = LocalDateTime.now().minusHours(1).toLocalTime();
        ReservationTime time = new ReservationTime(1L, oneHourAgo);

        assertThat(time.isPastOn(LocalDate.now())).isTrue();
    }

    @Test
    void 오늘이고_시작시간이_지금보다_이후이면_과거가_아니다() {
        LocalTime oneHourLater = LocalDateTime.now().plusHours(1).toLocalTime();
        ReservationTime time = new ReservationTime(1L, oneHourLater);

        assertThat(time.isPastOn(LocalDate.now())).isFalse();
    }
}

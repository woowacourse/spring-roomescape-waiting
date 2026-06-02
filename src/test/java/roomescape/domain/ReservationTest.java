package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private LocalDate date = LocalDate.parse("2026-05-05");
    private LocalTime startAt = LocalTime.parse("10:00");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void 이름이_null_또는_blank이면_예외(String name) {
        // given
        ReservationTime time = new ReservationTime(1L, startAt);
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new Reservation(null, name, date, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);
        ReservationTime time = new ReservationTime(1L, startAt);
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new Reservation(null, name, date, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 날짜_null로_예약_생성시_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, startAt);
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new Reservation(null, "구구", null, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("date는 비어 있을 수 없습니다.");
        ;
    }

    @Test
    void 예약_시간이_null이면_예약_생성시_예외() {
        // given
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new Reservation(null, "홍길동", date, null, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("time은 비어있을 수 없습니다.");
    }

    @Test
    void 테마가_null이면_예약_생성시_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, startAt);

        // when & then
        assertThatThrownBy(() -> new Reservation(null, "홍길동", date, time, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("theme는 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 255})
    void 예약_생성_성공_테스트(int count) {
        // given
        String name = "a".repeat(count);
        ReservationTime time = new ReservationTime(1L, startAt);
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");

        // when
        Reservation result = new Reservation(null, name, date, time, theme);

        // then
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void 예약자_이름이_같은지_확인한다() {
        // given
        Reservation reservation = reservation("브라운", date, new ReservationTime(1L, startAt));

        // when & then
        assertThat(reservation.isOwnedBy("브라운")).isTrue();
        assertThat(reservation.isOwnedBy("구구")).isFalse();
    }

    @Test
    void 지난_예약인지_확인한다() {
        // given
        Reservation reservation = reservation(
                "브라운",
                LocalDate.now().minusDays(1),
                new ReservationTime(1L, startAt));

        // when & then
        assertThat(reservation.isPast(LocalDateTime.now())).isTrue();
    }

    @Test
    void 예약_일정이_같은지_확인한다() {
        // given
        ReservationTime time = new ReservationTime(1L, startAt);
        Reservation reservation = reservation("브라운", date, time);
        Reservation sameSchedule = reservation("구구", date, time);
        Reservation differentSchedule = reservation("브라운", date.plusDays(1), time);

        // when & then
        assertThat(reservation.hasSameSchedule(sameSchedule)).isTrue();
        assertThat(reservation.hasSameSchedule(differentSchedule)).isFalse();
    }

    @Test
    void 예약_시간이_같은지_확인한다() {
        // given
        ReservationTime time = new ReservationTime(1L, startAt);
        Reservation reservation = reservation("브라운", date, time);

        // when & then
        assertThat(reservation.hasTime(time)).isTrue();
        assertThat(reservation.hasTime(new ReservationTime(2L, LocalTime.parse("11:00")))).isFalse();
    }

    private Reservation reservation(String name, LocalDate date, ReservationTime time) {
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");
        return new Reservation(null, name, date, time, theme);
    }
}

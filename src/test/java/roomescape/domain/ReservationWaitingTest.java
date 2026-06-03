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

class ReservationWaitingTest {
    private LocalDate date = LocalDate.parse("2026-05-05");
    private LocalTime startAt = LocalTime.parse("10:00");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void 이름이_null_또는_blank이면_예외(String name) {
        // given
        ReservationSlot slot = slot(date, new ReservationTime(1L, startAt));

        // when & then
        assertThatThrownBy(() -> new ReservationWaiting(null, name, slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);
        ReservationSlot slot = slot(date, new ReservationTime(1L, startAt));

        // when & then
        assertThatThrownBy(() -> new ReservationWaiting(null, name, slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 슬롯이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationWaiting(null, "홍길동", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("slot은 비어 있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 255})
    void 예약_생성_성공_테스트(int count) {
        // given
        String name = "a".repeat(count);
        ReservationTime time = new ReservationTime(1L, startAt);
        ReservationSlot slot = slot(date, time);

        // when
        ReservationWaiting result = new ReservationWaiting(null, name, slot);

        // then
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void 예약자_이름이_같은지_확인한다() {
        // given
        ReservationWaiting waiting = waiting("브라운", date, new ReservationTime(1L, startAt));

        // when & then
        assertThat(waiting.isOwnedBy("브라운")).isTrue();
        assertThat(waiting.isOwnedBy("구구")).isFalse();
    }

    @Test
    void 지난_예약인지_확인한다() {
        // given
        ReservationWaiting reservation = waiting(
                "브라운",
                LocalDate.now().minusDays(1),
                new ReservationTime(1L, startAt));

        // when & then
        assertThat(reservation.isPast(LocalDateTime.now())).isTrue();
    }

    private ReservationWaiting waiting(String name, LocalDate date, ReservationTime time) {
        return new ReservationWaiting(null, name, slot(date, time));
    }

    private ReservationSlot slot(LocalDate date, ReservationTime time) {
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");
        return new ReservationSlot(date, time, theme);
    }
}

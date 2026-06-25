package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationWaitingTest {
    private LocalDate date = LocalDate.parse("2026-05-05");
    private LocalTime startAt = LocalTime.parse("10:00");

    @Test
    void 예약자가_null이면_예외() {
        // given
        ReservationSlot slot = slot(date, new ReservationTime(1L, startAt));

        // when & then
        assertThatThrownBy(() -> new ReservationWaiting(null, null, slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reserver는 비어 있을 수 없습니다.");
    }

    @Test
    void 슬롯이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationWaiting(null, new Reserver("홍길동"), null))
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
        ReservationWaiting result = new ReservationWaiting(null, new Reserver(name), slot);

        // then
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void 예약자_이름이_같은지_확인한다() {
        // given
        ReservationWaiting waiting = waiting("브라운", date, new ReservationTime(1L, startAt));

        // when & then
        assertThat(waiting.isOwnedBy(new Reserver("브라운"))).isTrue();
        assertThat(waiting.isOwnedBy(new Reserver("구구"))).isFalse();
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

    @Test
    void 예약_대기를_예약으로_승격한다() {
        // given
        ReservationSlot slot = slot(date, new ReservationTime(1L, startAt));
        ReservationWaiting waiting = new ReservationWaiting(1L, new Reserver("브라운"), slot);

        // when
        Reservation result = waiting.promoteToReservation();

        // then
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo(waiting.getName());
        assertThat(result.getSlot()).isEqualTo(slot);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    private ReservationWaiting waiting(String name, LocalDate date, ReservationTime time) {
        return new ReservationWaiting(null, new Reserver(name), slot(date, time));
    }

    private ReservationSlot slot(LocalDate date, ReservationTime time) {
        Theme theme = new Theme(null, "테마 이름", "테마 설명", "썸네일");
        return new ReservationSlot(date, time, theme);
    }
}

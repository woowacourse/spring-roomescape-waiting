package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.PastDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotTest {

    @Test
    @DisplayName("현재 시간보다 미래인 슬롯은 시간 검증을 통과한다.")
    void validateAvailableTimeSuccessTest() {
        // given
        Schedule schedule = Schedule.from(
                LocalDate.of(2026, 6, 5),
                ReservationTime.from(1L, LocalTime.of(10, 0))
        );
        Theme theme = Theme.from(1L, "홍대 방탈출", "https://picsum.photos/seed/empty/400/300", "설명");
        Slot slot = Slot.from(schedule, theme);

        LocalDateTime now = LocalDateTime.of(2026, 6, 5, 9, 59);

        // when & then
        slot.validateAvailableTime(now);
    }

    @Test
    @DisplayName("현재 시간보다 이전인 슬롯의 가능 시간 여부 검증 시 예외를 발생시킨다.")
    void validateAvailableTimeExceptionTest() {
        // given
        Schedule schedule = Schedule.from(
                LocalDate.of(2026, 6, 5),
                ReservationTime.from(1L, LocalTime.of(10, 0))
        );
        Theme theme = Theme.from(1L, "홍대 방탈출", "https://picsum.photos/seed/empty/400/300", "설명");
        Slot slot = Slot.from(schedule, theme);

        LocalDateTime now = LocalDateTime.of(2026, 6, 5, 10, 1);

        // when & then
        assertThatThrownBy(() -> slot.validateAvailableTime(now))
                .isInstanceOf(PastDateTimeException.class)
                .hasMessage("과거의 날짜/시간입니다.");
    }

    @Test
    @DisplayName("스케줄과 테마가 모두 동일한 슬롯은 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        Schedule schedule1 = Schedule.from(
                LocalDate.of(2026, 6, 5),
                ReservationTime.from(1L, LocalTime.of(10, 0))
        );
        Schedule schedule2 = Schedule.from(
                LocalDate.of(2026, 6, 5),
                ReservationTime.from(1L, LocalTime.of(10, 0))
        );

        Theme theme1 = Theme.from(1L, "홍대 방탈출", "https://picsum.photos/seed/empty/400/300", "설명");
        Theme theme2 = Theme.from(1L, "홍대 방탈출", "https://picsum.photos/seed/empty/400/300", "설명");

        Slot slot1 = Slot.from(schedule1, theme1);
        Slot slot2 = Slot.from(schedule2, theme2);

        // when & then
        assertThat(slot1).isEqualTo(slot2);
        assertThat(slot1.hashCode()).isEqualTo(slot2.hashCode());
    }
}

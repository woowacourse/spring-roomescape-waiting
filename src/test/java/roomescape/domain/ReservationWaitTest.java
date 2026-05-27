package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationWaitTest {
    @Test
    void id는_0이하면_생성불가() {
        assertThatThrownBy(() -> new ReservationWait(-1L, 1L, 1L, LocalDateTime.of(2026, 5, 26, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void reservationId는_null_불가() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                null,
                1L,
                LocalDateTime.of(2026, 5, 26, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 null일 수 없습니다.");
    }

    @Test
    void reservationId는_0이하면_생성불가() {
        assertThatThrownBy(() -> new ReservationWait(1L, 0L, 1L, LocalDateTime.of(2026, 5, 26, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void memberId는_null_불가() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                null,
                LocalDateTime.of(2026, 5, 26, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 null일 수 없습니다.");
    }

    @Test
    void memberId는_0이하면_생성불가() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                0L,
                LocalDateTime.of(2026, 5, 26, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void createdAt은_null일_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                1L,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("생성 시간은 null일 수 없습니다.");
    }
}
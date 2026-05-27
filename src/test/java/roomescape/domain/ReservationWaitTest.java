package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ReservationWaitTest {

    private static final LocalDateTime SAMPLE_CREATED_AT = LocalDateTime.of(2026, 5, 26, 0, 0);

    @Test
    void id가_음수이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                -1L,
                1L,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void reservationId가_null이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                null,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 null일 수 없습니다.");
    }

    @Test
    void reservationId가_음수이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                -1L,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void memberId가_null이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                null,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 null일 수 없습니다.");
    }

    @Test
    void memberId가_음수이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                -1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 0보다 작거나 같을 수 없습니다.");
    }

    @Test
    void 생성시간이_null이면_예약대기를_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationWait(
                1L,
                1L,
                1L,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("생성 시간은 null일 수 없습니다.");
    }
}

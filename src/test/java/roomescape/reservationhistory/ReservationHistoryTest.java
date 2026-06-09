package roomescape.reservationhistory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ReservationHistoryTest {

    private static final LocalDate SAMPLE_DATE = LocalDate.of(2026, 12, 1);
    private static final LocalDateTime SAMPLE_CREATED_AT = LocalDateTime.of(2026, 5, 26, 0, 0);

    @Test
    void reservationId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                null,
                1L,
                SAMPLE_DATE,
                1L,
                1L,
                1L,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void memberId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                null,
                SAMPLE_DATE,
                1L,
                1L,
                1L,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void 날짜가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                null,
                1L,
                1L,
                1L,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 비어 있을 수 없습니다.");
    }

    @Test
    void timeId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                SAMPLE_DATE,
                null,
                1L,
                1L,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약시간 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void themeId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                SAMPLE_DATE,
                1L,
                null,
                1L,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void storeId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                SAMPLE_DATE,
                1L,
                1L,
                null,
                ReservationHistoryAction.CREATED,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 ID는 비어 있을 수 없습니다.");
    }

    @Test
    void action이_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                SAMPLE_DATE,
                1L,
                1L,
                1L,
                null,
                1L,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이력 액션은 비어 있을 수 없습니다.");
    }

    @Test
    void actorId가_null이면_이력을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationHistory(
                1L,
                1L,
                1L,
                SAMPLE_DATE,
                1L,
                1L,
                1L,
                ReservationHistoryAction.CREATED,
                null,
                SAMPLE_CREATED_AT
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수행자 ID는 비어 있을 수 없습니다.");
    }
}

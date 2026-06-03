package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WaitingLineTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final TimeSlot TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

    @Test
    @DisplayName("빈 대기 목록으로 대기 줄을 생성하면 예외가 발생한다.")
    void 빈_대기_목록_예외_발생() {
        assertThatThrownBy(() -> new WaitingLine(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기가 없습니다.");
    }

    @Test
    @DisplayName("null 대기 목록으로 대기 줄을 생성하면 예외가 발생한다.")
    void null_대기_목록_예외_발생() {
        assertThatThrownBy(() -> new WaitingLine(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기가 없습니다.");
    }

    @Test
    @DisplayName("생성 시각이 빠른 예약 대기가 우선 순번이다.")
    void 생성_시각_기준_대기_순번_계산() {
        Waiting first = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Waiting second = createWaiting(2L, "순번2", LocalDateTime.of(2026, 6, 3, 10, 1));
        WaitingLine waitingLine = new WaitingLine(List.of(second, first));

        assertThat(waitingLine.findWaitingNumber(first)).isEqualTo(1);
        assertThat(waitingLine.findWaitingNumber(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("생성 시각이 같으면 ID가 작은 예약 대기가 우선 순번이다.")
    void ID_기준_대기_순번_계산() {
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 6, 3, 10, 0);
        Waiting first = createWaiting(1L, "순번1", sameCreatedAt);
        Waiting second = createWaiting(2L, "순번2", sameCreatedAt);
        WaitingLine waitingLine = new WaitingLine(List.of(second, first));

        assertThat(waitingLine.findWaitingNumber(first)).isEqualTo(1);
        assertThat(waitingLine.findWaitingNumber(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("다른 예약 슬롯의 대기가 섞이면 예외가 발생한다.")
    void 다른_예약_슬롯_대기_예외_발생() {
        Waiting waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Waiting otherSlot = new Waiting(
                2L,
                "순번2",
                DATE.plusDays(1),
                TIME_SLOT,
                THEME,
                LocalDateTime.of(2026, 6, 3, 10, 1)
        );

        assertThatThrownBy(() -> new WaitingLine(List.of(waiting, otherSlot)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
    }

    @Test
    @DisplayName("null 예약 대기의 순번을 조회하면 예외가 발생한다.")
    void null_예약_대기_순번_조회_예외_발생() {
        Waiting waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        WaitingLine waitingLine = new WaitingLine(List.of(waiting));

        assertThatThrownBy(() -> waitingLine.findWaitingNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번 계산을 위해 예약 대기는 필수입니다.");
    }

    @Test
    @DisplayName("다른 예약 슬롯의 대기 순번을 조회하면 예외가 발생한다.")
    void 다른_예약_슬롯_대기_순번_조회_예외_발생() {
        Waiting waiting = createWaiting(1L, "순번1", LocalDateTime.of(2026, 6, 3, 10, 0));
        Waiting otherSlot = new Waiting(
                2L,
                "순번2",
                DATE.plusDays(1),
                TIME_SLOT,
                THEME,
                LocalDateTime.of(2026, 6, 3, 10, 1)
        );
        WaitingLine waitingLine = new WaitingLine(List.of(waiting));

        assertThatThrownBy(() -> waitingLine.findWaitingNumber(otherSlot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기 순번은 같은 예약 슬롯에 대해서만 계산 가능합니다.");
    }

    private Waiting createWaiting(Long id, String name, LocalDateTime createdAt) {
        return new Waiting(id, name, DATE, TIME_SLOT, THEME, createdAt);
    }
}

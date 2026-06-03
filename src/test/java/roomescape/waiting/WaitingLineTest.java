package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WaitingLineTest {

    @Test
    @DisplayName("가장 먼저 신청한 대기를 첫 번째 대기로 반환한다.")
    void 대기_순번_테스트_1() {
        Waiting first = Waiting.of(1L, 1L, 1L);
        Waiting second = Waiting.of(2L, 2L, 1L);
        WaitingLine waitingLine = WaitingLine.of(List.of(first, second));

        assertThat(waitingLine.first()).contains(first);
    }

    @Test
    @DisplayName("대기 순번은 신청 순서 기준으로 계산한다.")
    void orderOf() {
        Waiting first = Waiting.of(1L, 1L, 1L);
        Waiting second = Waiting.of(2L, 2L, 1L);
        WaitingLine waitingLine = WaitingLine.of(List.of(first, second));

        assertThat(waitingLine.orderOf(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("대기가 없으면 첫 번째 대기를 반환하지 않는다.")
    void emptyFirst() {
        WaitingLine waitingLine = WaitingLine.of(List.of());

        assertThat(waitingLine.first()).isEmpty();
    }
}

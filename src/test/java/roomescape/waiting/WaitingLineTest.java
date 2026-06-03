package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void 대기_순번_테스트_2() {
        Waiting first = Waiting.of(1L, 1L, 1L);
        Waiting second = Waiting.of(2L, 2L, 1L);
        WaitingLine waitingLine = WaitingLine.of(List.of(first, second));

        assertThat(waitingLine.orderOf(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("대기가 없으면 첫 번째 대기를 반환하지 않는다.")
    void 대기_순번_테스트_3() {
        WaitingLine waitingLine = WaitingLine.of(List.of());

        assertThat(waitingLine.first()).isEmpty();
    }

    @Test
    @DisplayName("서로 다른 슬롯의 대기로 대기열을 만들 수 없다.")
    void 대기_순번_테스트_4() {
        Waiting first = Waiting.of(1L, 1L, 1L);
        Waiting second = Waiting.of(2L, 2L, 2L);

        assertThatThrownBy(() -> WaitingLine.of(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

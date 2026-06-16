package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingLine;

public class WaitingLineTest {

    @Test
    @DisplayName("가장 먼저 신청한 대기를 첫 번째 대기로 반환한다.")
    void returns_earliest_waiting_as_first() {
        Waiting first = roomescape.TestFixtures.waiting(1L, 1L, 1L);
        Waiting second = roomescape.TestFixtures.waiting(2L, 2L, 1L);
        WaitingLine waitingLine = WaitingLine.of(List.of(first, second));

        assertThat(waitingLine.first()).contains(first);
    }

    @Test
    @DisplayName("대기 순번은 신청 순서 기준으로 계산한다.")
    void calculates_waiting_order_by_request_sequence() {
        Waiting first = roomescape.TestFixtures.waiting(1L, 1L, 1L);
        Waiting second = roomescape.TestFixtures.waiting(2L, 2L, 1L);
        WaitingLine waitingLine = WaitingLine.of(List.of(first, second));

        assertThat(waitingLine.orderOf(second)).isEqualTo(2);
    }

    @Test
    @DisplayName("대기가 없으면 첫 번째 대기를 반환하지 않는다.")
    void empty_waiting_line_returns_no_first_waiting() {
        WaitingLine waitingLine = WaitingLine.of(List.of());

        assertThat(waitingLine.first()).isEmpty();
    }

    @Test
    @DisplayName("서로 다른 슬롯의 대기로 대기열을 만들 수 없다.")
    void waitings_from_different_slots_cannot_create_waiting_line() {
        Waiting first = roomescape.TestFixtures.waiting(1L, 1L, 1L);
        Waiting second = roomescape.TestFixtures.waiting(2L, 2L, 2L);

        assertThatThrownBy(() -> WaitingLine.of(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

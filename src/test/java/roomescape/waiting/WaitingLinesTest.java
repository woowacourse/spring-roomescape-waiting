package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingLines;

class WaitingLinesTest {

    @Test
    @DisplayName("여러 슬롯의 대기열에서 특정 대기의 순번을 계산한다.")
    void calculates_order_of_waiting_across_multiple_waiting_lines() {
        Waiting firstSlotFirst = roomescape.TestFixtures.waiting(1L, 1L, 1L);
        Waiting firstSlotSecond = roomescape.TestFixtures.waiting(2L, 2L, 1L);
        Waiting secondSlotFirst = roomescape.TestFixtures.waiting(3L, 3L, 2L);
        Waiting secondSlotSecond = roomescape.TestFixtures.waiting(4L, 4L, 2L);
        WaitingLines waitingLines = WaitingLines.of(List.of(
                firstSlotFirst,
                firstSlotSecond,
                secondSlotFirst,
                secondSlotSecond
        ));

        assertThat(waitingLines.orderOf(firstSlotSecond)).isEqualTo(2L);
        assertThat(waitingLines.orderOf(secondSlotSecond)).isEqualTo(2L);
    }

    @Test
    @DisplayName("대기열에 없는 대기의 순번은 계산할 수 없다.")
    void waiting_not_in_line_cannot_have_order() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);
        WaitingLines waitingLines = WaitingLines.of(List.of(waiting));
        Waiting unknownWaiting = roomescape.TestFixtures.waiting(2L, 2L, 2L);

        assertThatThrownBy(() -> waitingLines.orderOf(unknownWaiting))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package roomescape.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.repository.history.MyWaitingOrder;
import roomescape.service.history.MyWaitingLines;

class MyWaitingLinesTest {

    @Test
    @DisplayName("여러 예약의 대기 줄을 예약별로 나누어 순번을 계산한다")
    void sequenceOf() {
        MyWaitingLines waitingLines = MyWaitingLines.from(List.of(
                waitingOrder(1L, 3L, "2026-08-05T12:01:00"),
                waitingOrder(1L, 2L, "2026-08-05T12:00:00"),
                waitingOrder(2L, 5L, "2026-08-05T12:03:00"),
                waitingOrder(2L, 4L, "2026-08-05T12:02:00")
        ));

        assertThat(waitingLines.sequenceOf(1L, 2L)).isOne();
        assertThat(waitingLines.sequenceOf(1L, 3L)).isEqualTo(2);
        assertThat(waitingLines.sequenceOf(2L, 4L)).isOne();
        assertThat(waitingLines.sequenceOf(2L, 5L)).isEqualTo(2);
    }

    private MyWaitingOrder waitingOrder(
            final Long reservationId,
            final Long waitingId,
            final String requestedAt
    ) {
        return new MyWaitingOrder(
                reservationId,
                reservationId,
                waitingId,
                LocalDateTime.parse(requestedAt)
        );
    }
}

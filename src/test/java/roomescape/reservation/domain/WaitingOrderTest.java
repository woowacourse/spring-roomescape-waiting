package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingOrderTest {

    @DisplayName("1씩 증가된 대기 순서 값을 가져온다")
    @Test
    void increased_waiting_order() {
        WaitingOrder waitingOrder = new WaitingOrder();

        long value1 = waitingOrder.issueNextWaitingOrder();
        long value2 = waitingOrder.issueNextWaitingOrder();

        assertThat(value2).isEqualTo(value1 + 1);
    }

    @DisplayName("대기 순서 값을 초기화 한다")
    @Test
    void reset_waiting_order() {
        WaitingOrder waitingOrder = new WaitingOrder();

        long value1 = waitingOrder.issueNextWaitingOrder();
        waitingOrder.issueNextWaitingOrder();
        waitingOrder.issueNextWaitingOrder();

        waitingOrder.resetWaitingOrder();
        long value2 = waitingOrder.issueNextWaitingOrder();

        assertThat(value1).isEqualTo(value2);
    }
}

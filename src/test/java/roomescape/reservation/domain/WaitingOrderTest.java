package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingOrderTest {

    @DisplayName("1씩 증가된 대기 순서 값을 가져온다")
    @Test
    void increased_order() {
        WaitingOrder waitingOrder = new WaitingOrder();

        assertThat(waitingOrder.issueNextWaitingOrder()).isEqualTo(waitingOrder.issueNextWaitingOrder() - 1);
    }
}

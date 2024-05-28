package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingOrderTest {

    @Test
    void 대기번호가_1보다_작을경우_예외_발생() {
        //when, then
        assertThatThrownBy(() -> new WaitingOrder(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 대기번호가_1보다_큰_경우만_하나_감소() {
        //given
        WaitingOrder waitingOrder = new WaitingOrder(2);

        // when
        waitingOrder.decreaseWaitingOrderByOne();
        int waitingOrder1 = waitingOrder.getWaitingOrder();

        waitingOrder.decreaseWaitingOrderByOne();
        int waitingOrder2 = waitingOrder.getWaitingOrder();

        //then
        assertAll(
                () -> assertThat(waitingOrder1).isEqualTo(1),
                () -> assertThat(waitingOrder2).isEqualTo(1)
        );
    }

    @Test
    void 대기번호가_첫번째일_경우_true() {
        //given
        WaitingOrder waitingOrder = new WaitingOrder(1);

        //when
        boolean result = waitingOrder.isFirstOrder();

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_대기번호보다_큰지_확인() {
        //given
        WaitingOrder waitingOrder = new WaitingOrder(2);

        //when
        boolean result = waitingOrder.isWaitingOrderGreaterThan(1);

        //when
        assertThat(result).isTrue();
    }
}

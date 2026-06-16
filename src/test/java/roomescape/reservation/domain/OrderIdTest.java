package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class OrderIdTest {

    @DisplayName("주문 ID 생성을 테스트합니다.")
    @Test
    void create_order_id() {
        OrderId orderId = OrderId.builder()
                .value("order-123_abc")
                .build();

        assertThat(orderId.value()).isEqualTo("order-123_abc");
    }

    @DisplayName("주문 ID 자동 생성을 테스트합니다.")
    @Test
    void generate_order_id() {
        OrderId orderId = OrderId.generate();

        assertThat(orderId.value())
                .startsWith("order-")
                .matches("^[A-Za-z0-9_-]{6,64}$");
    }

    @DisplayName("올바르지 않은 주문 ID 생성 시 예외를 테스트합니다.")
    @NullAndEmptySource
    @ValueSource(strings = {"short", "order id", "order#123"})
    @ParameterizedTest
    void create_invalid_order_id_exception(String value) {
        assertThatThrownBy(() -> OrderId.builder()
                .value(value)
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("주문 ID는 6~64자의 영숫자, -, _만 사용할 수 있습니다.");
    }
}

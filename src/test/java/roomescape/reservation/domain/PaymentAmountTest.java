package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class PaymentAmountTest {

    @DisplayName("결제 금액 생성을 테스트합니다.")
    @Test
    void create_payment_amount() {
        PaymentAmount amount = PaymentAmount.builder()
                .value(50_000L)
                .build();

        assertThat(amount.value()).isEqualTo(50_000L);
    }

    @DisplayName("양수가 아닌 결제 금액 생성 시 예외를 테스트합니다.")
    @NullSource
    @ValueSource(longs = {0L, -1L, -10_000L})
    @ParameterizedTest
    void create_non_positive_payment_amount_exception(Long value) {
        assertThatThrownBy(() -> PaymentAmount.builder()
                .value(value)
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("결제 금액은 양수여야 합니다.");
    }
}

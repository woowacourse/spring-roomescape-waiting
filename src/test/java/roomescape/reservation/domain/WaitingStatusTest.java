package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.DomainValidationException;

class WaitingStatusTest {

    @DisplayName("에약 대기 번호는 5번을 넘어갈 수 없다")
    @ParameterizedTest
    @ValueSource(ints = {6, 7, 8, 9, 10})
    void should_waiting_number_less_than_five(int waitingNumber) {
        assertThatThrownBy(() -> new WaitingStatus(waitingNumber))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("예약 대기 번호는 음수일 수 없다")
    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -3, -4})
    void should_waiting_number_positive(int negativeWaitingNumber) {
        assertThatThrownBy(() -> new WaitingStatus(negativeWaitingNumber))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("예약 대기 번호가 0인 경우는 예약이 완료된 상태이다")
    @Test
    void should_waiting_number_zero_is_reserved_status() {
        assertThat(new WaitingStatus(0).isWaiting()).isFalse();
    }

    @DisplayName("예약 대기 랭크를 올리면 예약 대기 순서가 1 감소한다")
    @Test
    void should_decrease_waiting_number_when_rank_up() {
        assertThat(new WaitingStatus(1).rankUp()).isEqualTo(new WaitingStatus(0));
    }
}

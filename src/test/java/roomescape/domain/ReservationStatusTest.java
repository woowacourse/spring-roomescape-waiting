package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new ReservationStatus(Status.RESERVED, 0L))
                .doesNotThrowAnyException();
    }

    @DisplayName("대기 번호가 음수일 경우 예외가 발생한다.")
    @Test
    void createFail_NegativePriority() {
        assertThatThrownBy(() -> new ReservationStatus(Status.RESERVED, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 상태인데 , 대기 번호가 존재할 경우 예외가 발생한다.")
    @Test
    void createFail_ReservedHasPriority() {
        assertThatThrownBy(() -> new ReservationStatus(Status.RESERVED, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약대기 상태인데 , 대기 번호가 존재하지 않을 경우 예외가 발생한다.")
    @Test
    void createFail_NotReservedHasNotPriority() {
        assertThatThrownBy(() -> new ReservationStatus(Status.WAITING, 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("대기 번호가 하나 줄어든다.")
    @Test
    void updateDecreasedPriorityStatus() {
        ReservationStatus reservationStatus = new ReservationStatus(Status.WAITING, 1L);

        ReservationStatus actual = reservationStatus.updateDecreasedPriorityStatus();
        ReservationStatus expected = new ReservationStatus(Status.RESERVED, 0L);

        assertThat(actual).isEqualTo(expected);
    }
}

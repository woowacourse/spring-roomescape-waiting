package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;

class WaitingTest {

    @DisplayName("예약 대기 상태를 변경한다.")
    @ParameterizedTest
    @CsvSource({"APPROVED", "DENIED"})
    void updateWaitingStatus(WaitingStatus changeStatus) {

        // given
        Waiting waiting = new Waiting(1L, LocalDate.now(), null, null, null, WaitingStatus.PENDING);

        // when
        waiting.updateWaiting(changeStatus);

        // then
        assertThat(waiting.getStatus()).isEqualTo(changeStatus);
    }

    @DisplayName("예약 대기 상태가 PENDING이 아니라면 변경할 수 없다.")
    @ParameterizedTest
    @CsvSource({"APPROVED", "DENIED"})
    void updateWaitingStatus_throwsException(WaitingStatus status) {

        // given
        Waiting waiting = new Waiting(1L, LocalDate.now(), null, null, null, status);

        // when & then
        assertThatThrownBy(() -> waiting.updateWaiting(WaitingStatus.APPROVED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ExceptionCause.WAITING_STATUS_ALREADY_UPDATED.getMessage());
    }

    @DisplayName("예약 대기 상태를 PENDING으로 변경할 수 없다.")
    @Test
    void updateWaitingStatus_throwsException2() {
        // given
        Waiting waiting = new Waiting(1L, LocalDate.now(), null, null, null, WaitingStatus.PENDING);

        // when & then
        assertThatThrownBy(() -> waiting.updateWaiting(WaitingStatus.PENDING))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ExceptionCause.WAITING_STATUS_ALREADY_UPDATED.getMessage());
    }
}
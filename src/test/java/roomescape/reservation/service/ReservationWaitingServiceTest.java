package roomescape.reservation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.reservation.dto.ReservationWaitingDto;
import roomescape.reservation.dto.SaveReservationWaitingRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @DisplayName("사용자의 예약 대기 정보를 모두 조회한다.")
    @Test
    void getMyReservationWaitingTest() {
        // Given
        final Long memberId = 1L;

        // When
        final List<ReservationWaitingDto> myReservationWaiting = reservationWaitingService.getMyReservationWaiting(memberId);

        // Then
        assertAll(
                () -> assertThat(myReservationWaiting).hasSize(2),
                () -> assertThat(myReservationWaiting.get(0).order()).isEqualTo(1)
        );
    }

    @DisplayName("예약 대기를 생성한다.")
    @Test
    void saveReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now().plusDays(3), 2L, 1L, 10L);

        // When
        final Long reservationWaiting = reservationWaitingService.saveReservationWaiting(saveReservationWaitingRequest);

        // Then
        assertThat(reservationWaiting).isEqualTo(7L);
    }

    @DisplayName("존재하지 않는 예약에 대해 대기 신청을 하면 예외가 발생한다.")
    @Test
    void saveNotExistReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now(), 1L, 1L, 2L);

        // When & Then
        assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(saveReservationWaitingRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("존재하지 않는 예약에 대한 대기 신청을 할 수 없습니다.");
    }

    @DisplayName("중복된 예약 대기 신청을 하면 예외가 발생한다.")
    @Test
    void saveDuplicateReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now().plusDays(6), 2L, 3L, 8L);

        // When & Then
        assertThatThrownBy(() -> reservationWaitingService.saveReservationWaiting(saveReservationWaitingRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 해당 예약 대기가 존재합니다.");
    }

    @DisplayName("예약 대기 정보를 삭제한다.")
    @Test
    void deleteReservationWaitingTest() {
        // Given
        final Long reservationWaitingId = 2L;

        // When & Then
        Assertions.assertThatCode(() -> reservationWaitingService.deleteReservationWaiting(reservationWaitingId))
                .doesNotThrowAnyException();
    }
}

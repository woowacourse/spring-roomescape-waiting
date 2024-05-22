package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.TestFixture;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    @DisplayName("예약 대기 목록을 조회한다.")
    void findReservationWaitings() {
        // given
        final Reservation reservation = new Reservation(TestFixture.MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH),
                RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.WAITING);
        given(reservationRepository.findByStatus(ReservationStatus.WAITING))
                .willReturn(List.of(reservation));

        // when
        final List<ReservationResponse> actual = waitingService.findReservationWaitings();

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("예약 대기를 승인한다.")
    void approveReservationWaiting() {
        // given
        final Reservation waiting = new Reservation(1L, TestFixture.MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH),
                RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.WAITING);
        given(reservationRepository.findById(waiting.getId())).willReturn(Optional.of(waiting));
        given(reservationRepository.existsByThemeAndDateAndTimeAndStatus(waiting.getTheme(), waiting.getDate(),
                waiting.getTime(), ReservationStatus.RESERVED))
                .willReturn(false);

        // when
        waitingService.approveReservationWaiting(waiting.getId());

        // then
        assertThat(waiting.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("이미 예약이 있는 상태에서 승인을 할 경우 예외가 발생한다.")
    void throwExceptionWhenAlreadyExistsReservation() {
        // given
        final Reservation waiting = new Reservation(1L, TestFixture.MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH),
                RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.WAITING);
        given(reservationRepository.findById(waiting.getId())).willReturn(Optional.of(waiting));
        given(reservationRepository.existsByThemeAndDateAndTimeAndStatus(waiting.getTheme(), waiting.getDate(),
                waiting.getTime(), ReservationStatus.RESERVED))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingService.approveReservationWaiting(waiting.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("예약 대기를 거절한다.")
    void rejectReservationWaiting() {
        // given
        final Reservation waiting = new Reservation(1L, TestFixture.MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH),
                RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.WAITING);
        given(reservationRepository.existsById(waiting.getId()))
                .willReturn(true);

        // when & then
        assertThatCode(() -> waitingService.rejectReservationWaiting(waiting.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Id에 해당하는 예약 대기가 없으면 예외가 발생한다.")
    void throwExceptionWhenRejectNotExistingReservationWaiting() {
        // given
        final Long notExistingId = 0L;
        given(reservationRepository.existsById(notExistingId))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingService.rejectReservationWaiting(notExistingId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

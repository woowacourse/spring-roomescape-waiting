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
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingService waitingService;
    
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

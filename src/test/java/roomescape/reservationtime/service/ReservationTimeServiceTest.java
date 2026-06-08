package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("존재하지 않는 id로 시간 조회 시 예외 발생")
    void getById_없으면_예외() {
        when(timeRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.getById(Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND))
                .hasMessage(ErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약이 존재하는 시간은 삭제할 수 없다")
    void 예약_있는_시간_삭제_불가() {
        when(timeRepository.hasReservation(1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.TIME_HAS_RESERVATION))
                .hasMessage(ErrorCode.TIME_HAS_RESERVATION.getMessage());
    }
}

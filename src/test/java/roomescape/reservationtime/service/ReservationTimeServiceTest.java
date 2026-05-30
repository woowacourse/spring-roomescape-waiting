package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFactory;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ReservationTimeFactory reservationTimeFactory;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    private final ReservationTime time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));

    @Test
    @DisplayName("시간 생성 성공")
    void 시간_생성_성공() {
        when(reservationTimeFactory.create(any(), any())).thenReturn(time);
        when(timeRepository.save(any())).thenReturn(time);

        TimeResponse response = reservationTimeService.createTime(new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("전체 시간 조회")
    void 전체_시간_조회() {
        when(timeRepository.findAll()).thenReturn(List.of(time));

        assertThat(reservationTimeService.getAllTimes()).hasSize(1);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void 예약_가능_시간_조회() {
        when(timeRepository.findAvailableByDateAndThemeId(any(), any())).thenReturn(List.of(time));

        assertThat(reservationTimeService.getAvailableTimes(LocalDate.now(), 1L)).hasSize(1);
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        when(timeRepository.existsReservationByTimeId(1L)).thenReturn(false);

        reservationTimeService.deleteById(1L);
        verify(timeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("id로 시간 조회 성공")
    void getById_성공() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));

        assertThat(reservationTimeService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id로 시간 조회 시 예외 발생")
    void getById_없으면_예외() {
        when(timeRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.getById(4L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND));
    }

    @Test
    @DisplayName("예약이 존재하는 시간은 삭제할 수 없다")
    void 예약_있는_시간_삭제_불가() {
        when(timeRepository.existsReservationByTimeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_HAS_RESERVATION));
    }
}

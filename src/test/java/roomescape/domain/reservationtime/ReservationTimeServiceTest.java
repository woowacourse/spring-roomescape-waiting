package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.reservationtime.dto.TimeRequest;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void createTime_정상_생성() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0));
        ReservationTime saved = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(timeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(false);
        when(timeRepository.save(any(ReservationTime.class))).thenReturn(saved);

        TimeResponse response = reservationTimeService.createTime(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @Test
    void createTime_중복된_시작_시간이면_예외() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(timeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_TIME);
    }

    @Test
    void getAllTimes_정상_조회() {
        ReservationTime t1 = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ReservationTime t2 = ReservationTime.of(2L, LocalTime.of(11, 0), LocalTime.of(12, 0));
        when(timeRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TimeResponse> responses = reservationTimeService.getAllTimes();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(TimeResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void deleteById_정상_삭제() {
        when(timeRepository.existsById(1L)).thenReturn(true);

        reservationTimeService.deleteById(1L);

        verify(timeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_존재하지_않는_id면_예외() {
        when(timeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservationTimeService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_ID_NOT_FOUND);
    }

    @Test
    void deleteById_예약이_있어_DB_제약_위반이_발생하면_예외() {
        when(timeRepository.existsById(1L)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(timeRepository).deleteById(1L);

        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_DELETE_NOT_ALLOWED);
    }
}

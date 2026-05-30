package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.dto.ReservationTimeCommand;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간 생성 시, 기존에 이미 동일한 시간이 있으면 예외가 발생한다.")
    void registerReservationTime_duplicate() {
        //given
        given(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0)))
                .willReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationTimeService.save(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        )).isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.DUPLICATE_TIME.getMessage());
    }

    @Test
    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    void deleteById_nonExistentTime_throwsNotFoundException() {
        //given
        given(reservationTimeRepository.deleteById(1L))
                .willReturn(0);

        //when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TimeErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 시간 삭제시, 예약 시간이 사용 중이면 예외가 발생한다.")
    void deleteById_timeInUse_throwsDeleteFailedException() {
        //given
        given(reservationTimeRepository.deleteById(1L))
                .willThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.TIME_IN_USE.getMessage());
    }
}

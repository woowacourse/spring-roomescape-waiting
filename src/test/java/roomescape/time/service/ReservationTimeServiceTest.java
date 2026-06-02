package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.DuplicateTimeException;
import roomescape.time.exception.TimeInUseException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.dto.ReservationTimeCommand;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    ThemeRepository themeRepository;

    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @Mock
    Clock clock;

    @InjectMocks
    ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간 생성 시, 기존에 이미 동일한 시간이 있으면 예외가 발생한다.")
    @Test
    void registerReservationTime_duplicate() {
        //given
        when(reservationTimeRepository.existByStartAt(LocalTime.of(10, 0)))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationTimeService.registerReservationTime(
                new ReservationTimeCommand(LocalTime.of(10, 0))
        )).isInstanceOf(DuplicateTimeException.class);
    }

    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    @Test
    void removeThemeById_not_found() {
        //given
        when(reservationTimeRepository.findById(1L))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationTimeService.removeReservationTimeById(1L))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약 시간 삭제시, 예약 시간이 사용 중이면 예외가 발생한다.")
    @Test
    void removeThemeById_in_use() {
        //given
        when(reservationTimeRepository.findById(1L))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(reservationTimeRepository.deleteById(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.removeReservationTimeById(1L))
                .isInstanceOf(TimeInUseException.class);
    }
}

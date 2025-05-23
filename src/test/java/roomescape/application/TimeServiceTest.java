package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
public class TimeServiceTest {

    @InjectMocks
    private TimeService timeService;

    @Mock
    private TimeRepository timeRepository;

    @Test
    public void getTimeById() {
        // given
        Long id = 1L;
        ReservationTime expectedTime = ReservationTime.of(id, LocalTime.of(10, 0));

        Mockito.doReturn(Optional.of(expectedTime)).when(timeRepository).findById(id);

        // when
        ReservationTime reservationTime = timeService.getTimeEntityById(id);

        // then
        assertThat(reservationTime).isEqualTo(expectedTime);
    }
}

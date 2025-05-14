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
import roomescape.application.dto.TimeDto;
import roomescape.domain.ReservationTime;
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
        TimeDto expectedTime = new TimeDto(id, LocalTime.of(10, 0));
        ReservationTime reservationTime = expectedTime.toEntity();

        Mockito.doReturn(Optional.of(reservationTime)).when(timeRepository).findById(id);

        // when
        TimeDto timeById = timeService.getTimeById(id);

        // then
        assertThat(timeById.id()).isEqualTo(id);
    }
}

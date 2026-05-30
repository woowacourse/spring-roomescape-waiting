package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceMockTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void getById는_존재하지_않으면_NotFoundException을_던진다() {
        given(timeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_존재하면_시간을_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(timeRepository.findById(1L)).willReturn(Optional.of(time));

        assertThat(reservationTimeService.getById(1L)).isEqualTo(time);
    }

    @Test
    void getReservationTimes는_저장소의_전체_시간을_반환한다() {
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        given(timeRepository.findAll()).willReturn(times);

        assertThat(reservationTimeService.getReservationTimes()).isEqualTo(times);
    }
}

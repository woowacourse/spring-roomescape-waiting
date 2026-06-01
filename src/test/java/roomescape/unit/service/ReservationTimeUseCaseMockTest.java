package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.ReservationTimeApplicationService;
import roomescape.application.service.ReservationQueryService;
import roomescape.application.service.ReservationTimeCommandService;
import roomescape.application.service.ReservationTimeQueryService;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.projection.ReservationTimeAvailability;
import roomescape.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeUseCaseMockTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ReservationTimeCommandService reservationTimeCommandService;

    private ReservationTimeQueryService reservationTimeQueryService;
    private ReservationTimeApplicationService reservationTimeApplicationService;

    @BeforeEach
    void setUp() {
        reservationTimeQueryService = new ReservationTimeQueryService(timeRepository);
        reservationTimeApplicationService = new ReservationTimeApplicationService(
                reservationTimeCommandService,
                reservationTimeQueryService,
                reservationQueryService
        );
    }

    @Test
    void getById는_존재하지_않으면_NotFoundException을_던진다() {
        given(timeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeQueryService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_존재하면_시간을_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(timeRepository.findById(1L)).willReturn(Optional.of(time));

        assertThat(reservationTimeQueryService.getById(1L)).isEqualTo(time);
    }

    @Test
    void findAll은_저장소의_전체_시간을_반환한다() {
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        given(timeRepository.findAll()).willReturn(times);

        assertThat(reservationTimeQueryService.findAll()).isEqualTo(times);
    }

    @Test
    void findWithAvailability는_저장소의_시간별_예약_상태를_반환한다() {
        LocalDate date = LocalDate.of(2026, 8, 5);
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        given(timeRepository.findAll()).willReturn(times);
        given(reservationQueryService.findReservedTimeIds(date, 1L)).willReturn(Set.of(1L));

        List<ReservationTimeAvailability> availabilities = reservationTimeApplicationService.findWithAvailability(
                date,
                1L
        );

        assertThat(availabilities)
                .extracting(ReservationTimeAvailability::available)
                .containsExactly(false, true);
    }
}

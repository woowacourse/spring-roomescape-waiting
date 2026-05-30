package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

public class ReservationTimeServiceTest {
    private ReservationTimeService reservationTimeService;
    private ReservationTimeRepository reservationTimeRepository;
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        reservationTimeRepository = Mockito.mock(ReservationTimeRepository.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, clock);
    }

    @Test
    void saveTest() {
        ServiceReservationTimeCreateRequest request = new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0));

        ReservationTime reservationTimeWithoutId = request.toEntity();
        ReservationTime reservationTime = ReservationTime.of(1L, reservationTimeWithoutId);

        when(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(false);
        when(reservationTimeRepository.save(reservationTimeWithoutId)).thenReturn(reservationTime);

        assertThat(reservationTimeService.save(request)).isEqualTo(reservationTime);
    }

    @Test
    void saveExceptionTest() {
        ServiceReservationTimeCreateRequest request = new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0));

        when(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.save(request))
                .isInstanceOf(CustomInvalidRequestException.class);
    }

    @Test
    void findAllTest() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );

        when(reservationTimeRepository.findAll()).thenReturn(reservationTimes);

        assertThat(reservationTimeService.findAll()).isEqualTo(reservationTimes);
    }

    @Test
    void validateNotPastDateExceptionTest() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() -> reservationTimeService.validateNotPastDate(date))
                .isInstanceOf(CustomInvalidRequestException.class);
    }

    @Test
    void deleteTest() {
        reservationTimeService.delete(1L);

        verify(reservationTimeRepository, times(1)).delete(1L);
    }

    @Test
    void findReservationTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));

        assertThat(reservationTimeService.findReservationTime(1L)).isEqualTo(reservationTime);
    }

    @Test
    void findReservationExceptionTest() {
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.findReservationTime(1L))
                .isInstanceOf(CustomInvalidRequestException.class);
    }
}

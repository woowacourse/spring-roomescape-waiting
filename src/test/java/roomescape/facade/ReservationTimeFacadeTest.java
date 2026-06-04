package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.dto.ReservationTimeDto;
import roomescape.repository.dto.ThemeDto;
import roomescape.repository.dto.WaitDetailDto;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

public class ReservationTimeFacadeTest {

    private ReservationTimeService reservationTimeService;
    private ReservationService reservationService;
    private ThemeService themeService;
    private WaitService waitService;
    private ReservationTimeFacade reservationTimeFacade;

    private LocalDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationTimeService = Mockito.mock(ReservationTimeService.class);
        reservationService = Mockito.mock(ReservationService.class);
        themeService = Mockito.mock(ThemeService.class);
        waitService = Mockito.mock(WaitService.class);

        reservationDate = LocalDate.of(2026, 5, 3);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");

        reservationTimeFacade = new ReservationTimeFacade(reservationTimeService, reservationService, themeService,
                waitService);
    }

    @Test
    void saveTest() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime reservationTime = ReservationTime.of(1L, reservationTimeWithoutId);

        ServiceReservationTimeCreateRequest request = new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0));
        ServiceReservationTimeResponse response = ServiceReservationTimeResponse.from(reservationTime);

        when(reservationTimeService.save(reservationTimeWithoutId)).thenReturn(reservationTime);

        assertThat(reservationTimeFacade.save(request)).isEqualTo(response);
    }

    @Test
    void findAllTest() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );

        List<ServiceReservationTimeResponse> responses = List.of(
                ServiceReservationTimeResponse.from(reservationTimes.get(0)),
                ServiceReservationTimeResponse.from(reservationTimes.get(1))
        );

        when(reservationTimeService.findAll()).thenReturn(reservationTimes);

        assertThat(reservationTimeFacade.findAll()).isEqualTo(responses);
    }

    @Test
    void findAvailabilityByDateAndThemeTest() {
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.of(11, 0));
        ReservationTime thirdTime = new ReservationTime(3L, LocalTime.of(12, 0));

        List<ReservationTime> allReservationTimes = List.of(firstTime, secondTime, thirdTime);
        List<ReservationTime> reservedTimes = List.of(secondTime, thirdTime);

        List<WaitDetailDto> thirdTimeWaits = List.of(
                new WaitDetailDto(1L, LocalDateTime.now(), "fizz", reservationDate, ReservationTimeDto.from(thirdTime),
                        ThemeDto.from(theme), 1L),
                new WaitDetailDto(2L, LocalDateTime.now(), "luke", reservationDate, ReservationTimeDto.from(thirdTime),
                        ThemeDto.from(theme), 2L),
                new WaitDetailDto(3L, LocalDateTime.now(), "neo", reservationDate, ReservationTimeDto.from(thirdTime),
                        ThemeDto.from(theme), 3L)
        );

        List<ServiceReservationTimeAvailabilityResponse> responses = List.of(
                ServiceReservationTimeAvailabilityResponse.from(firstTime,
                        ReservationAvailability.RESERVATION_AVAILABLE),
                ServiceReservationTimeAvailabilityResponse.from(secondTime, ReservationAvailability.WAITING_AVAILABLE),
                ServiceReservationTimeAvailabilityResponse.from(thirdTime, ReservationAvailability.NOTHING_AVAILABLE)
        );

        when(reservationTimeService.findReservedTimesByDateAndTheme(reservationDate, theme.getId())).thenReturn(
                reservedTimes);
        when(reservationTimeService.findAll()).thenReturn(allReservationTimes);
        when(waitService.findBySlot(reservationDate, thirdTime.getId(), theme.getId())).thenReturn(thirdTimeWaits);

        assertThat(reservationTimeFacade.findAvailabilityByDateAndTheme(reservationDate, theme.getId())).isEqualTo(
                responses);

        verify(themeService, times(1)).validateExistTheme(theme.getId());
    }

    @Test
    void deleteTest() {
        reservationTimeFacade.delete(reservationTime.getId());

        verify(reservationService, times(1)).validateReferencedTime(reservationTime.getId());
        verify(reservationTimeService, times(1)).delete(reservationTime.getId());
    }
}

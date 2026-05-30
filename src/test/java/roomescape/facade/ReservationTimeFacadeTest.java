package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.controller.dto.response.ReservationTimeAvailabilityResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

public class ReservationTimeFacadeTest {

    private ReservationTimeService reservationTimeService;
    private ReservationService reservationService;
    private ThemeService themeService;
    private ReservationTimeFacade reservationTimeFacade;

    private LocalDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationTimeService = Mockito.mock(ReservationTimeService.class);
        reservationService = Mockito.mock(ReservationService.class);
        themeService = Mockito.mock(ThemeService.class);

        reservationDate = LocalDate.of(2026, 5, 3);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");

        reservationTimeFacade = new ReservationTimeFacade(reservationTimeService, reservationService, themeService);
    }

    @Test
    void saveTest() {
        ServiceReservationTimeCreateRequest request = new ServiceReservationTimeCreateRequest(LocalTime.of(10, 0));

        when(reservationTimeService.save(request)).thenReturn(reservationTime);

        assertThat(reservationTimeFacade.save(request)).isEqualTo(ReservationTimeResponse.from(reservationTime));
    }

    @Test
    void findAllTest() {
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.of(11, 0));

        when(reservationTimeService.findAll()).thenReturn(List.of(firstTime, secondTime));

        assertThat(reservationTimeFacade.findAll()).isEqualTo(List.of(
                ReservationTimeResponse.from(firstTime),
                ReservationTimeResponse.from(secondTime)
        ));
    }

    @Test
    void findAvailabilityByDateAndThemeTest() {
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.of(11, 0));

        when(reservationTimeService.findAll()).thenReturn(List.of(firstTime, secondTime));
        when(reservationTimeService.findReservedTimeIdsByDateAndTheme(reservationDate, theme.getId()))
                .thenReturn(List.of(secondTime.getId()));

        assertThat(reservationTimeFacade.findAvailabilityByDateAndTheme(reservationDate, theme.getId())).isEqualTo(
                List.of(
                        ReservationTimeAvailabilityResponse.from(firstTime, true),
                        ReservationTimeAvailabilityResponse.from(secondTime, false)
                ));

        verify(themeService, times(1)).validateExistTheme(theme.getId());
    }

    @Test
    void deleteTest() {
        reservationTimeFacade.delete(reservationTime.getId());

        verify(reservationService, times(1)).validateReferencedTime(reservationTime.getId());
        verify(reservationTimeService, times(1)).delete(reservationTime.getId());
    }
}

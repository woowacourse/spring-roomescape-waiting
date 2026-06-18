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
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeAvailabilityListResponse;
import roomescape.controller.dto.response.ReservationTimeAvailabilityResponse;
import roomescape.controller.dto.response.ReservationTimeListResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.Member;
import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

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
        ReservationTime reservationTime = ReservationTime.withId(1L, reservationTimeWithoutId);

        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        ReservationTimeResponse response = ReservationTimeResponse.from(reservationTime);

        when(reservationTimeService.save(reservationTimeWithoutId)).thenReturn(reservationTime);

        assertThat(reservationTimeFacade.save(request)).isEqualTo(response);
    }

    @Test
    void findAllTest() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );

        ReservationTimeListResponse response = ReservationTimeListResponse.from(reservationTimes);

        when(reservationTimeService.findAll()).thenReturn(reservationTimes);

        assertThat(reservationTimeFacade.findAll()).isEqualTo(response);
    }

    @Test
    void findAvailabilityByDateAndThemeTest() {
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.of(11, 0));
        ReservationTime thirdTime = new ReservationTime(3L, LocalTime.of(12, 0));

        List<ReservationTime> allReservationTimes = List.of(firstTime, secondTime, thirdTime);
        List<ReservationTime> reservedTimes = List.of(secondTime, thirdTime);

        Slot thirdSlot = new Slot(reservationDate, thirdTime, theme);
        Waits thirdTimeWaits = new Waits(List.of(
                new Wait(1L, LocalDateTime.of(2026, 5, 3, 10, 0), new Member(1L, "fizz"), thirdSlot),
                new Wait(2L, LocalDateTime.of(2026, 5, 3, 10, 1), new Member(2L, "luke"), thirdSlot),
                new Wait(3L, LocalDateTime.of(2026, 5, 3, 10, 2), new Member(3L, "neo"), thirdSlot)
        ));

        List<ReservationTimeAvailabilityResponse> availabilityResponses = List.of(
                ReservationTimeAvailabilityResponse.from(firstTime,
                        ReservationAvailability.RESERVATION_AVAILABLE),
                ReservationTimeAvailabilityResponse.from(secondTime, ReservationAvailability.WAITING_AVAILABLE),
                ReservationTimeAvailabilityResponse.from(thirdTime, ReservationAvailability.NOTHING_AVAILABLE)
        );

        ReservationTimeAvailabilityListResponse response = ReservationTimeAvailabilityListResponse.from(
                availabilityResponses);

        when(reservationTimeService.findReservedTimesByDateAndTheme(reservationDate, theme.getId())).thenReturn(
                reservedTimes);
        when(reservationTimeService.findAll()).thenReturn(allReservationTimes);
        when(waitService.findBySlot(new Slot(reservationDate, secondTime, theme)))
                .thenReturn(new Waits(List.of()));
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(waitService.findBySlot(thirdSlot)).thenReturn(thirdTimeWaits);

        assertThat(reservationTimeFacade.findAvailabilityByDateAndTheme(reservationDate, theme.getId())).isEqualTo(
                response);

        verify(themeService, times(1)).validateExistTheme(theme.getId());
    }

    @Test
    void deleteTest() {
        reservationTimeFacade.delete(reservationTime.getId());

        verify(reservationService, times(1)).validateReferencedTime(reservationTime.getId());
        verify(reservationTimeService, times(1)).delete(reservationTime.getId());
    }
}

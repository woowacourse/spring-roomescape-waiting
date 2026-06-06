package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.response.ReservationListResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.ReservationWaitListResponse;
import roomescape.controller.dto.response.WaitListResponse;
import roomescape.controller.dto.response.WaitResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.ReservationTimeInfo;
import roomescape.service.dto.ThemeInfo;
import roomescape.service.dto.WaitInfo;

public class ReservationFacadeTest {

    private ReservationService reservationService;
    private WaitService waitService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private Clock clock;
    private ReservationFacade reservationFacade;

    private LocalDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;
    private LocalDateTime now;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        waitService = Mockito.mock(WaitService.class);
        reservationTimeService = Mockito.mock(ReservationTimeService.class);
        themeService = Mockito.mock(ThemeService.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        reservationDate = LocalDate.of(2026, 5, 3);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
        now = LocalDateTime.now(clock);

        reservationFacade = new ReservationFacade(reservationService, waitService, reservationTimeService, themeService,
                clock);
    }

    @Test
    void saveReservationTest() {
        ReservationCreateRequest request = new ReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation reservation = Reservation.withId(1L, reservationWithoutId);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.empty());
        when(reservationService.save(reservationWithoutId)).thenReturn(reservation);

        assertThat(reservationFacade.save(request)).isEqualTo(ReservationResponse.from(reservation));
    }

    @Test
    void saveWaitTest() {
        ReservationCreateRequest request = new ReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "luke", reservationDate, reservationTime, theme);
        Wait waitWithoutId = request.toWait(now, reservationTime, theme);
        Wait wait = Wait.withId(1L, waitWithoutId);
        WaitResponse response = WaitResponse.from(WaitInfo.of(wait, 1L));

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));
        when(waitService.save(waitWithoutId)).thenReturn(WaitInfo.of(wait, 1L));
        when(waitService.calculateOrder(wait)).thenReturn(1L);

        assertThat(reservationFacade.save(request)).isEqualTo(response);
    }

    @Test
    void savePastTimeReservationCreateExceptionTest() {
        ReservationCreateRequest request = new ReservationCreateRequest("fizz", LocalDate.of(2026, 3, 20),
                reservationTime.getId(), theme.getId());

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);

        assertThatThrownBy(() -> reservationFacade.save(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE.getMessage());
    }

    @Test
    void saveDuplicateReservationExceptionTest() {
        ReservationCreateRequest request = new ReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));

        assertThatThrownBy(() -> reservationFacade.save(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.DUPLICATED_RESERVATION.getMessage());
    }

    @Test
    void findByNameTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(new Reservation(1L, "fizz", reservationDate, reservationTime, theme));
        List<WaitInfo> waits = List.of(new WaitInfo(1L, "fizz", otherDate,
                ReservationTimeInfo.from(reservationTime), ThemeInfo.from(theme), ReservationStatus.WAITING, 1L,
                LocalDateTime.now(clock)));

        ReservationListResponse reservationListResponse = ReservationListResponse.from(reservations);
        WaitListResponse waitListResponse = WaitListResponse.from(waits);

        ReservationWaitListResponse result = new ReservationWaitListResponse(reservationListResponse, waitListResponse);

        when(reservationService.findByName("fizz")).thenReturn(reservations);
        when(waitService.findByName("fizz")).thenReturn(waits);

        assertThat(reservationFacade.findByName("fizz")).isEqualTo(result);
    }

    @Test
    void findAllTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", reservationDate, reservationTime, theme),
                new Reservation(2L, "luke", otherDate, reservationTime, theme)
        );

        List<WaitInfo> waits = List.of(
                new WaitInfo(1L, "fizz", otherDate, ReservationTimeInfo.from(reservationTime), ThemeInfo.from(theme),
                        ReservationStatus.WAITING, 1L, LocalDateTime.now(clock)),
                new WaitInfo(1L, "luke", reservationDate, ReservationTimeInfo.from(reservationTime),
                        ThemeInfo.from(theme),
                        ReservationStatus.WAITING, 1L, LocalDateTime.now(clock))
        );

        ReservationListResponse reservationListResponse = ReservationListResponse.from(reservations);
        WaitListResponse waitListResponse = WaitListResponse.from(waits);

        ReservationWaitListResponse result = new ReservationWaitListResponse(reservationListResponse, waitListResponse);

        when(reservationService.findAll()).thenReturn(reservations);
        when(waitService.findAll()).thenReturn(waits);

        assertThat(reservationFacade.findAll()).isEqualTo(result);
    }

    @Test
    void deleteReservationWithoutWaitTest() {
        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(List.of());

        reservationFacade.deleteReservation(reservation.getId());

        verify(reservationService, times(1)).delete(reservation.getId());
    }

    @Test
    void deleteReservationWithWaitTest() {
        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);
        Wait firstWait = new Wait(1L, now, "luke", reservationDate, reservationTime, theme);
        Reservation newReservationWithoutId = new Reservation(firstWait.getName(), firstWait.getReservationDate(),
                firstWait.getTime(), firstWait.getTheme());

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(List.of(WaitInfo.of(firstWait, 1L)));

        reservationFacade.deleteReservation(reservation.getId());
        verify(reservationService, times(1)).save(newReservationWithoutId);
        verify(reservationService, times(1)).delete(reservation.getId());
        verify(waitService, times(1)).delete(firstWait.getId());
    }

    @Test
    void deleteReservationExceptionTest() {
        LocalDate pastReservationDate = LocalDate.of(2026, 3, 20);
        Reservation reservation = new Reservation(1L, "fizz", pastReservationDate, reservationTime, theme);

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);

        assertThatThrownBy(() -> reservationFacade.deleteReservation(reservation.getId()))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE.getMessage());
    }

    @Test
    void deleteWaitTest() {
        Wait wait = new Wait(1L, now, "fizz", reservationDate, reservationTime, theme);

        when(waitService.findWait(wait.getId())).thenReturn(WaitInfo.of(wait, 1L));

        reservationFacade.deleteWait(wait.getId());

        verify(waitService, times(1)).delete(wait.getId());
    }
}

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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.dto.ReservationTimeDto;
import roomescape.repository.dto.ThemeDto;
import roomescape.repository.dto.WaitDetailDto;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.response.ServiceReceptionListResponse;
import roomescape.service.dto.response.ServiceReservationResponse;
import roomescape.service.dto.response.ServiceWaitResponse;

public class ReceptionFacadeTest {

    private ReservationService reservationService;
    private WaitService waitService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private Clock clock;
    private ReceptionFacade receptionFacade;

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

        receptionFacade = new ReceptionFacade(reservationService, waitService, reservationTimeService, themeService,
                clock);
    }

    @Test
    void saveReservationTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation reservation = Reservation.of(1L, reservationWithoutId);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.empty());
        when(reservationService.save(reservationWithoutId)).thenReturn(reservation);

        assertThat(receptionFacade.save(request)).isEqualTo(ServiceReservationResponse.from(reservation));
    }

    @Test
    void saveWaitTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "luke", reservationDate, reservationTime, theme);
        Wait waitWithoutId = request.toWait(now, reservationTime, theme);
        Wait wait = Wait.of(1L, waitWithoutId);
        ServiceWaitResponse response = ServiceWaitResponse.from(WaitDetailDto.from(wait, 1L));

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));
        when(waitService.save(waitWithoutId)).thenReturn(WaitDetailDto.from(wait, 1L));
        when(waitService.calculateOrder(wait)).thenReturn(1L);

        assertThat(receptionFacade.save(request)).isEqualTo(response);
    }

    @Test
    void savePastTimeReservationCreateExceptionTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 3, 20),
                reservationTime.getId(), theme.getId());

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);

        assertThatThrownBy(() -> receptionFacade.save(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE.getMessage());
    }

    @Test
    void saveDuplicateReservationExceptionTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));

        assertThatThrownBy(() -> receptionFacade.save(request))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.DUPLICATED_RESERVATION.getMessage());
    }

    @Test
    void findByNameTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(new Reservation(1L, "fizz", reservationDate, reservationTime, theme));
        List<WaitDetailDto> waits = List.of(new WaitDetailDto(1L, LocalDateTime.now(clock), "fizz", otherDate,
                ReservationTimeDto.from(reservationTime), ThemeDto.from(theme), 1L));

        ServiceReceptionListResponse result = ServiceReceptionListResponse.from(reservations, waits);

        when(reservationService.findByName("fizz")).thenReturn(reservations);
        when(waitService.findByName("fizz")).thenReturn(waits);

        assertThat(receptionFacade.findByName("fizz")).isEqualTo(result);
    }

    @Test
    void findAllTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", reservationDate, reservationTime, theme),
                new Reservation(2L, "luke", otherDate, reservationTime, theme)
        );

        List<WaitDetailDto> waits = List.of(
                new WaitDetailDto(1L, LocalDateTime.now(clock), "fizz", otherDate,
                        ReservationTimeDto.from(reservationTime), ThemeDto.from(theme), 1L),
                new WaitDetailDto(2L, LocalDateTime.now(clock), "luke", reservationDate,
                        ReservationTimeDto.from(reservationTime), ThemeDto.from(theme), 1L));

        ServiceReceptionListResponse result = ServiceReceptionListResponse.from(reservations, waits);

        when(reservationService.findAll()).thenReturn(reservations);
        when(waitService.findAll()).thenReturn(waits);

        assertThat(receptionFacade.findAll()).isEqualTo(result);
    }

    @Test
    void deleteReservationWithoutWaitTest() {
        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(List.of());

        receptionFacade.deleteReservation(reservation.getId());

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
                reservation.getTheme().getId())).thenReturn(List.of(WaitDetailDto.from(firstWait, 1L)));

        receptionFacade.deleteReservation(reservation.getId());
        verify(reservationService, times(1)).save(newReservationWithoutId);
        verify(reservationService, times(1)).delete(reservation.getId());
        verify(waitService, times(1)).delete(firstWait.getId());
    }

    @Test
    void deleteReservationExceptionTest() {
        LocalDate pastReservationDate = LocalDate.of(2026, 3, 20);
        Reservation reservation = new Reservation(1L, "fizz", pastReservationDate, reservationTime, theme);

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);

        assertThatThrownBy(() -> receptionFacade.deleteReservation(reservation.getId()))
                .isInstanceOf(CustomInvalidRequestException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE.getMessage());
    }

    @Test
    void deleteWaitTest() {
        Wait wait = new Wait(1L, now, "fizz", reservationDate, reservationTime, theme);

        when(waitService.findWait(wait.getId())).thenReturn(WaitDetailDto.from(wait, 1L));

        receptionFacade.deleteWait(wait.getId());

        verify(waitService, times(1)).delete(wait.getId());
    }
}

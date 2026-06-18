package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.exception.custom.AlreadyReservedException;
import roomescape.exception.custom.CannotCreatePastReservationException;
import roomescape.exception.custom.CannotDeletePastReservationException;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

public class ReservationFacadeTest {

    private ReservationService reservationService;
    private WaitService waitService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private MemberService memberService;
    private Clock clock;
    private ReservationFacade reservationFacade;

    private LocalDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;
    private LocalDateTime now;
    private Member fizz;
    private Member luke;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        waitService = Mockito.mock(WaitService.class);
        reservationTimeService = Mockito.mock(ReservationTimeService.class);
        themeService = Mockito.mock(ThemeService.class);
        memberService = Mockito.mock(MemberService.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        reservationDate = LocalDate.of(2026, 5, 3);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
        now = LocalDateTime.now(clock);
        fizz = new Member(1L, "fizz");
        luke = new Member(2L, "luke");

        reservationFacade = new ReservationFacade(reservationService, waitService, reservationTimeService, themeService,
                memberService, clock);
    }

    @Test
    void saveReservationTest() {
        ReservationCreateRequest request = new ReservationCreateRequest(reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation reservationWithoutId = request.toReservation(fizz, reservationTime, theme);
        Reservation reservation = reservationWithoutId.withId(1L);

        when(memberService.findMember(1L)).thenReturn(fizz);
        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.empty());
        when(reservationService.save(reservationWithoutId, false)).thenReturn(reservation);

        assertThat(reservationFacade.save(request, 1L)).isEqualTo(ReservationResponse.from(reservation));
    }

    @Test
    void saveWaitTest() {
        ReservationCreateRequest request = new ReservationCreateRequest(reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, luke, new Slot(reservationDate, reservationTime, theme));
        Wait waitWithoutId = request.toWait(now, fizz, reservationTime, theme);
        Wait wait = waitWithoutId.withId(1L);
        WaitResponse response = WaitResponse.of(wait, 1L);

        when(memberService.findMember(1L)).thenReturn(fizz);
        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));
        when(waitService.save(waitWithoutId)).thenReturn(wait);
        when(waitService.calculateOrder(wait)).thenReturn(1L);

        assertThat(reservationFacade.save(request, 1L)).isEqualTo(response);
    }

    @Test
    void savePastTimeReservationCreateExceptionTest() {
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2026, 3, 20),
                reservationTime.getId(), theme.getId());
        Reservation reservationWithoutId = request.toReservation(fizz, reservationTime, theme);

        when(memberService.findMember(1L)).thenReturn(fizz);
        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.empty());
        when(reservationService.save(reservationWithoutId, false)).thenThrow(
                new CannotCreatePastReservationException());

        assertThatThrownBy(() -> reservationFacade.save(request, 1L))
                .isInstanceOf(CannotCreatePastReservationException.class);
    }

    @Test
    void saveDuplicateReservationExceptionTest() {
        ReservationCreateRequest request = new ReservationCreateRequest(reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, fizz, new Slot(reservationDate, reservationTime, theme));

        when(memberService.findMember(1L)).thenReturn(fizz);
        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.findBySlot(request.date(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation));

        assertThatThrownBy(() -> reservationFacade.save(request, 1L))
                .isInstanceOf(AlreadyReservedException.class);
    }

    @Test
    void findByNameTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(
                new Reservation(1L, fizz, new Slot(reservationDate, reservationTime, theme)));

        Wait fizzWait = new Wait(1L, now, fizz, new Slot(otherDate, reservationTime, theme));
        Wait lukeWait = new Wait(2L, now, luke, new Slot(reservationDate, reservationTime, theme));
        Waits allWaits = new Waits(List.of(fizzWait, lukeWait));

        WaitResponse waitResponse = WaitResponse.of(fizzWait, 1L);
        WaitListResponse waitListResponse = new WaitListResponse(List.of(waitResponse));
        ReservationListResponse reservationListResponse = ReservationListResponse.from(reservations);
        ReservationWaitListResponse result = new ReservationWaitListResponse(reservationListResponse, waitListResponse);

        when(reservationService.findByName("fizz")).thenReturn(reservations);
        when(waitService.findAll()).thenReturn(allWaits);

        assertThat(reservationFacade.findByName("fizz")).isEqualTo(result);
    }

    @Test
    void findAllTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        List<Reservation> reservations = List.of(
                new Reservation(1L, fizz, new Slot(reservationDate, reservationTime, theme)),
                new Reservation(2L, luke, new Slot(otherDate, reservationTime, theme))
        );

        Wait fizzWait = new Wait(1L, now, fizz, new Slot(otherDate, reservationTime, theme));
        Wait lukeWait = new Wait(2L, now, luke, new Slot(reservationDate, reservationTime, theme));
        Waits allWaits = new Waits(List.of(fizzWait, lukeWait));

        WaitResponse fizzResponse = WaitResponse.of(fizzWait, 1L);
        WaitResponse lukeResponse = WaitResponse.of(lukeWait, 1L);
        WaitListResponse waitListResponse = new WaitListResponse(List.of(fizzResponse, lukeResponse));
        ReservationListResponse reservationListResponse = ReservationListResponse.from(reservations);
        ReservationWaitListResponse result = new ReservationWaitListResponse(reservationListResponse, waitListResponse);

        when(reservationService.findAll()).thenReturn(reservations);
        when(waitService.findAll()).thenReturn(allWaits);

        assertThat(reservationFacade.findAll()).isEqualTo(result);
    }

    @Test
    void deleteReservationWithoutWaitTest() {
        Reservation reservation = new Reservation(1L, fizz, new Slot(reservationDate, reservationTime, theme));

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getSlot())).thenReturn(new Waits(List.of()));

        reservationFacade.deleteReservation(reservation.getId());

        verify(reservationService, times(1)).delete(reservation, false);
    }

    @Test
    void deleteReservationWithWaitTest() {
        Reservation reservation = new Reservation(1L, fizz, new Slot(reservationDate, reservationTime, theme));
        Wait firstWait = new Wait(1L, now, luke, new Slot(reservationDate, reservationTime, theme));
        Reservation newReservationWithoutId = new Reservation(firstWait.getMember(),
                new Slot(firstWait.getReservationDate(), firstWait.getTime(), firstWait.getTheme()));

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getSlot())).thenReturn(new Waits(List.of(firstWait)));

        reservationFacade.deleteReservation(reservation.getId());

        verify(reservationService, times(1)).save(newReservationWithoutId, true);
        verify(reservationService, times(1)).deleteAndFlush(reservation, false);
        verify(waitService, times(1)).delete(firstWait.getId(), true);
    }

    @Test
    void deleteReservationExceptionTest() {
        LocalDate pastReservationDate = LocalDate.of(2026, 3, 20);
        Reservation reservation = new Reservation(1L, fizz, new Slot(pastReservationDate, reservationTime, theme));

        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getSlot())).thenReturn(new Waits(List.of()));
        doThrow(new CannotDeletePastReservationException()).when(reservationService).delete(reservation, false);

        assertThatThrownBy(() -> reservationFacade.deleteReservation(reservation.getId()))
                .isInstanceOf(CannotDeletePastReservationException.class);
    }

    @Test
    void deleteWaitTest() {
        Wait wait = new Wait(1L, now, fizz, new Slot(reservationDate, reservationTime, theme));

        when(waitService.findWait(wait.getId())).thenReturn(wait);

        reservationFacade.deleteWait(wait.getId());

        verify(waitService, times(1)).delete(wait.getId(), false);
    }
}

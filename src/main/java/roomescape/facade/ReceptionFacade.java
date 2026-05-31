package roomescape.facade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

@Service
@Transactional(readOnly = true)
public class ReceptionFacade {

    private final ReservationService reservationService;
    private final WaitService waitService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final Clock clock;

    public ReceptionFacade(ReservationService reservationService, WaitService waitService,
                           ReservationTimeService reservationTimeService, ThemeService themeService, Clock clock) {
        this.reservationService = reservationService;
        this.waitService = waitService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.clock = clock;
    }

    @Transactional
    public ReceptionResponse save(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.findReservationTime(request.timeId());
        Theme theme = themeService.findTheme(request.themeId());
        Reservation newReservation = new Reservation(request.name(), request.reservationDate(), reservationTime, theme);

        if (newReservation.isPast(LocalDateTime.now(clock))) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_CREATE);
        }

        return saveReservationOrWait(request, reservationTime, theme);
    }

    public List<ReceptionResponse> findByName(String name) {
        List<ReceptionResponse> receptions = new ArrayList<>();

        reservationService.findByName(name).stream()
                .map(r -> ReceptionResponse.from(r, 0L, ReservationStatus.CONFIRMED.name()))
                .forEach(receptions::add);

        waitService.findByName(name).stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    public List<ReceptionResponse> findAll() {
        List<ReceptionResponse> receptions = new ArrayList<>();

        reservationService.findAll().stream()
                .map(r -> ReceptionResponse.from(r, 0L, ReservationStatus.CONFIRMED.name()))
                .forEach(receptions::add);

        waitService.findAll().stream()
                .map(w -> ReceptionResponse.from(w, waitService.calculateOrder(w), ReservationStatus.WAITING.name()))
                .forEach(receptions::add);

        return receptions;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationService.findReservation(id);
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_DELETE);
        }
        reservationService.delete(id);

        List<Wait> waits = waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        if (waits.isEmpty()) {
            return;
        }
        confirmFirstWait(waits.getFirst());
    }

    @Transactional
    public void deleteWait(Long id) {
        Wait wait = waitService.findWait(id);
        if (wait.isPast(LocalDateTime.now(clock))) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_DELETE);
        }
        waitService.delete(id);
    }

    private ReceptionResponse saveReservationOrWait(ServiceReservationCreateRequest request,
                                                    ReservationTime reservationTime, Theme theme) {
        Optional<Reservation> existing = reservationService.findBySlot(request.reservationDate(), request.timeId(),
                request.themeId());
        if (existing.isEmpty()) {
            Reservation saved = reservationService.save(request, reservationTime, theme);
            return ReceptionResponse.from(saved, 0L, ReservationStatus.CONFIRMED.name());
        }
        if (existing.get().getName().equals(request.name())) {
            throw new RoomEscapeException(DomainErrorCode.DUPLICATED_RESERVATION);
        }

        Wait newWait = waitService.save(request.toWait(LocalDateTime.now(clock), reservationTime, theme));
        return ReceptionResponse.from(newWait, waitService.calculateOrder(newWait), ReservationStatus.WAITING.name());
    }

    private void confirmFirstWait(Wait firstOrder) {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest(firstOrder.getName(),
                firstOrder.getReservationDate(), firstOrder.getTime().getId(), firstOrder.getTheme().getId());

        reservationService.save(request, firstOrder.getTime(), firstOrder.getTheme());
        waitService.delete(firstOrder.getId());
    }
}
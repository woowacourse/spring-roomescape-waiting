package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.*;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateSessionException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.SessionNotFoundException;
import roomescape.repository.SessionRepository;
import roomescape.service.dto.AvailableTimeSlot;

import java.util.Objects;
import roomescape.service.dto.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TimeSlotService timeSlotService;
    private final ThemeService themeService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public SessionService(SessionRepository sessionRepository, TimeSlotService timeSlotService,
                          ThemeService themeService, ReservationService reservationService,
                          WaitingService waitingService) {
        this.sessionRepository = sessionRepository;
        this.timeSlotService = timeSlotService;
        this.themeService = themeService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public List<Session> allSessions() {
        return sessionRepository.findAll();
    }

    public Session findSessionById(long id) {
        return sessionRepository.findById(id).orElseThrow(SessionNotFoundException::new);
    }

    @Transactional
    public Session createSession(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = timeSlotService.findTimeSlotById(timeId);
        Theme theme = themeService.findThemeById(themeId);
        if (sessionRepository.findByDateAndTimeSlotIdAndThemeId(date, timeId, themeId).isPresent()) {
            throw new DuplicateSessionException(date, timeId, themeId);
        }
        return sessionRepository.save(Session.transientOf(date, timeSlot, theme));
    }

    @Transactional
    public List<Session> createSessionsForDate(LocalDate date) {
        List<TimeSlot> timeSlots = timeSlotService.allTimes();
        List<Theme> themes = themeService.allTheme();
        return timeSlots.stream()
                .flatMap(timeSlot -> themes.stream()
                        .map(theme -> sessionRepository.findByDateAndTimeSlotIdAndThemeId(
                                        date, timeSlot.getId(), theme.getId())
                                .orElseGet(() -> sessionRepository.save(Session.transientOf(date, timeSlot, theme)))))
                .toList();
    }

    public List<Reservation> allReservations() {
        return reservationService.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationService.findById(id);
    }

    public List<Booking> findReservationByName(String name) {
        List<Booking> bookings = new ArrayList<>();
        reservationService.findByName(name).forEach(r -> bookings.add(Booking.fromReservation(r)));
        waitingService.findByName(name).forEach(w -> bookings.add(Booking.fromWaiting(w)));
        return bookings;
    }

    public List<AvailableTimeSlot> findAvailableTimes(long themeId, LocalDate date) {
        themeService.findThemeById(themeId);
        return timeSlotService.findAvailableTimes(themeId, date);
    }

    @Transactional
    public Reservation makeReservation(ReservationRequest request) {
        Session session = findSessionOrThrow(request.date(), request.timeId(), request.themeId());
        return reservationService.save(request.name(), session);
    }

    @Transactional
    public void cancelReservation(long reservationId, String userName) {
        Reservation reservation = reservationService.findById(reservationId);
        reservation.validateModifiable(userName, LocalDateTime.now());
        reservationService.delete(reservationId);
        promoteWaitingIfExists(reservation.getSession());
    }

    @Transactional
    public Reservation rescheduleReservation(long id, String userName, ReservationRequest request) {
        Reservation existing = reservationService.findById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        Session newSession = findSessionOrThrow(request.date(), request.timeId(), request.themeId());
        reservationService.checkDuplicateForUpdate(newSession, id);
        Reservation updated = reservationService.save(existing.reschedule(newSession, LocalDateTime.now()));
        if (!existing.getSession().getId().equals(newSession.getId())) {
            promoteWaitingIfExists(existing.getSession());
        }
        return updated;
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation existing = reservationService.findById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        LocalDate date = Objects.requireNonNullElse(request.date(), existing.getSession().getDate());
        Long timeId = Objects.requireNonNullElse(request.timeId(), existing.getSession().getTimeSlot().getId());
        Long themeId = Objects.requireNonNullElse(request.themeId(), existing.getSession().getTheme().getId());
        Session targetSession = findSessionOrThrow(date, timeId, themeId);
        reservationService.checkDuplicateForUpdate(targetSession, id);
        Reservation updated = reservationService.save(existing.reschedule(targetSession, LocalDateTime.now()));
        if (!existing.getSession().getId().equals(targetSession.getId())) {
            promoteWaitingIfExists(existing.getSession());
        }
        return updated;
    }

    @Transactional
    public Waiting addWaiting(WaitingRequest request) {
        Session session = findSessionOrThrow(request.date(), request.timeId(), request.themeId());
        Reservation reservation = reservationService.findBySessionOrThrow(session);
        if (reservation.isReservedBy(request.name())) {
            throw new DuplicateReservationException(
                    session.getDate().toString(), session.getTimeSlot().getId(), session.getTheme().getId());
        }
        Waiting waiting = Waiting.transientOf(request.name(), session);
        waitingService.validateNotDuplicate(waiting);
        waiting.validateNotPast(LocalDateTime.now());
        return waitingService.save(waiting);
    }

    @Transactional
    public void cancelWaiting(long waitingId, String userName) {
        Waiting waiting = waitingService.findByIdOrThrow(waitingId);
        waiting.validateModifiable(userName, LocalDateTime.now());
        waitingService.deleteById(waitingId);
    }

    private Session findSessionOrThrow(LocalDate date, Long timeId, Long themeId) {
        return sessionRepository.findByDateAndTimeSlotIdAndThemeId(date, timeId, themeId)
                .orElseThrow(SessionNotFoundException::new);
    }

    private void promoteWaitingIfExists(Session session) {
        List<Waiting> waitings = waitingService.findBySession(session);
        session.promoteCandidate(waitings).ifPresent(candidate -> {
            waitingService.deleteById(candidate.getId());
            reservationService.save(candidate.getName(), session);
        });
    }
}

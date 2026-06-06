package roomescape.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.Booking;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final SessionService sessionService;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                              SessionService sessionService) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        Session session = sessionService.resolveNewSession(request.date(), request.timeId(), request.themeId());
        Reservation reservation = Reservation.transientOf(request.name(), session);
        reservation.validateNotPast(LocalDateTime.now());
        checkDuplicateForSave(session);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    public List<Booking> findReservationByName(String name) {
        List<Booking> bookings = new ArrayList<>();
        reservationRepository.findByName(name).forEach(r -> bookings.add(Booking.fromReservation(r)));
        waitingRepository.findByName(name).forEach(w -> bookings.add(Booking.fromWaiting(w)));
        return bookings;
    }

    @Transactional
    public void removeReservation(long id, String userName) {
        Reservation reservation = validModifiable(id, userName);
        reservationRepository.deleteById(id);
        processOldSession(reservation.getSession());
    }

    @Transactional
    public Reservation putReservation(long id, String userName, ReservationRequest request) {
        Reservation existing = validModifiable(id, userName);
        Session newSession = sessionService.resolveNewSession(request.date(), request.timeId(), request.themeId());
        return updateValidReservation(id, newSession, existing);
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation existing = validModifiable(id, userName);
        Session targetSession = sessionService.findSessionOrNull(request.date(), request.timeId(), request.themeId());
        Session persistentSession = sessionService.resolveSession(targetSession);
        return updateValidReservation(id, persistentSession, existing);
    }

    private Reservation updateValidReservation(long id, Session session, Reservation existing) {
        checkDuplicateForUpdate(session, id);
        Reservation updated = reservationRepository.update(existing.reschedule(session, LocalDateTime.now()));
        processOldSessionIfChanged(existing.getSession(), session);
        return updated;
    }

    private void processOldSessionIfChanged(Session oldSession, Session newSession) {
        if (!oldSession.getId().equals(newSession.getId())) {
            processOldSession(oldSession);
        }
    }

    private void processOldSession(Session session) {
        if (waitingRepository.isExistsBySlotId(session.getId())) {
            promoteFirstWaiting(session);
            return;
        }
        sessionService.deleteSession(session.getId());
    }

    private void promoteFirstWaiting(Session session) {
        Waiting firstWaiting = waitingRepository.findFirstBySlotId(session.getId());
        waitingRepository.deleteById(firstWaiting.getId());
        reservationRepository.save(Reservation.transientOf(firstWaiting.getName(), session));
    }

    private Reservation validModifiable(long id, String userName) {
        Reservation existing = findReservationById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        return existing;
    }

    private void checkDuplicateForSave(Session session) {
        if (reservationRepository.findByDateAndTimeIdAndThemeId(session.getDate(), session.getTimeSlot().getId(),
                session.getTheme().getId()).isPresent()) {
            throw new DuplicateReservationException(session.getDate().toString(), session.getTimeSlot().getId(),
                    session.getTheme().getId());
        }
    }

    private void checkDuplicateForUpdate(Session session, long targetId) {
        reservationRepository.findByDateAndTimeIdAndThemeId(session.getDate(), session.getTimeSlot().getId(),
                        session.getTheme().getId())
                .filter(existing -> !existing.getId().equals(targetId))
                .ifPresent(existing -> {
                    throw new DuplicateReservationException(session.getDate().toString(), session.getTimeSlot().getId(),
                            session.getTheme().getId());
                });
    }
}

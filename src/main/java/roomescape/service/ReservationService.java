package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
    }

    public Optional<Reservation> findBySession(Session session) {
        return reservationRepository.findBySession(session);
    }

    public Reservation findBySessionOrThrow(Session session) {
        return findBySession(session).orElseThrow(InvalidWaitingPrerequisiteException::new);
    }

    @Transactional
    public Reservation save(String name, Session session) {
        Reservation reservation = Reservation.transientOf(name, session);
        reservation.validateNotPast(LocalDateTime.now());
        checkDuplicateForSave(session);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void delete(long id) {
        reservationRepository.deleteById(id);
    }

    public void checkDuplicateForSave(Session session) {
        if (findBySession(session).isPresent()) {
            throw new DuplicateReservationException(
                    session.getDate().toString(), session.getTimeSlot().getId(), session.getTheme().getId());
        }
    }

    public void checkDuplicateForUpdate(Session session, long targetId) {
        findBySession(session)
                .filter(existing -> !existing.getId().equals(targetId))
                .ifPresent(existing -> {
                    throw new DuplicateReservationException(
                            session.getDate().toString(), session.getTimeSlot().getId(), session.getTheme().getId());
                });
    }
}

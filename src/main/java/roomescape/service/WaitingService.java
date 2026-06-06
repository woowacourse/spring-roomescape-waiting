package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final SessionService sessionService;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          SessionService sessionService) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public Waiting saveWaiting(WaitingRequest request) {
        Session session = sessionService.findSessionOrNull(request.date(), request.timeId(), request.themeId());
        validPrerequisite(session);
        Reservation reservation = findReservationOrThrow(session);
        validNotReservedBySelf(reservation, request.name());

        Waiting waiting = Waiting.transientOf(request.name(), session);
        validDuplicated(waiting);
        waiting.validateNotPast(LocalDateTime.now());

        return waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow(() -> new WaitingNotFoundException(id));
        waiting.validateModifiable(userName, LocalDateTime.now());
        waitingRepository.deleteById(id);
    }

    private void validPrerequisite(Session session) {
        if (session == null) {
            throw new InvalidWaitingPrerequisiteException();
        }
    }

    private Reservation findReservationOrThrow(Session session) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(session.getDate(), session.getTimeSlot().getId(),
                        session.getTheme().getId())
                .orElseThrow(InvalidWaitingPrerequisiteException::new);
    }

    private void validNotReservedBySelf(Reservation reservation, String userName) {
        if (reservation.getName().equals(userName)) {
            throw new DuplicateReservationException(
                    reservation.getSession().getDate().toString(),
                    reservation.getSession().getTimeSlot().getId(),
                    reservation.getSession().getTheme().getId()
            );
        }
    }

    private void validDuplicated(Waiting waiting) {
        if (waitingRepository.isExists(waiting)) {
            throw new DuplicateWaitingException(waiting);
        }
    }
}

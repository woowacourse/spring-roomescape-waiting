package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationScheduler {

    private final ReservationCommandRepository reservationCommandRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final WaitingCommandRepository waitingCommandRepository;
    private final WaitingQueryRepository waitingQueryRepository;
    private final Clock clock;

    public ReservationScheduler(ReservationCommandRepository reservationCommandRepository,
                                ReservationQueryRepository reservationQueryRepository,
                                WaitingCommandRepository waitingCommandRepository,
                                WaitingQueryRepository waitingQueryRepository,
                                Clock clock) {
        this.reservationCommandRepository = reservationCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.waitingCommandRepository = waitingCommandRepository;
        this.waitingQueryRepository = waitingQueryRepository;
        this.clock = clock;
    }

    @Transactional
    public void cancel(Long reservationId) {
        Reservation reservation = reservationQueryRepository.getById(reservationId);
        if (reservation.isPast(clock)) {
            throw new RoomescapeException(RoomescapeErrorCode.DATE_EXPIRED);
        }
        reservationCommandRepository.delete(reservation);
        reserveCandidateWaitingIfPresent(reservation);
    }

    private void reserveCandidateWaitingIfPresent(Reservation reservation) {
        LocalDate date = reservation.getDate();
        Time time = reservation.getTime();
        Theme theme = reservation.getTheme();
        waitingQueryRepository.findCandidateWaiting(date, time, theme)
                .ifPresent(this::replaceWaitingToReservation);
    }

    private void replaceWaitingToReservation(Waiting waiting) {
        saveWaitingToReservation(waiting);
        waitingCommandRepository.delete(waiting);
    }

    private void saveWaitingToReservation(Waiting waiting) {
        Reservation reservation = waiting.toReservation();
        reservationCommandRepository.save(reservation);
    }
}

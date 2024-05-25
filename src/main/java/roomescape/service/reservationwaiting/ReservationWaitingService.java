package roomescape.service.reservationwaiting;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.reservationwaiting.CannotDeleteOtherMemberWaiting;
import roomescape.exception.reservationwaiting.DuplicatedReservationWaitingException;
import roomescape.exception.reservationwaiting.InvalidDateTimeWaitingException;
import roomescape.exception.reservationwaiting.NotFoundReservationWaitingException;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;

@Service
@Transactional
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public Long saveReservationWaiting(ReservationWaitingRequest request, Member member) {
        Reservation reservation = findReservationByDateAndTimeIdAndThemeId(
                request.getDate(), request.getTimeId(), request.getThemeId());
        validateDuplicateWaiting(reservation, member);
        validateDateTimeWaiting(reservation);

        ReservationWaiting reservationWaiting = new ReservationWaiting(reservation, member);
        ReservationWaiting savedReservationWaiting = reservationWaitingRepository.save(reservationWaiting);
        return savedReservationWaiting.getId();
    }

    private void validateDuplicateWaiting(Reservation reservation, Member member) {
        if (reservationWaitingRepository.existsByReservationAndMember(reservation, member)) {
            throw new DuplicatedReservationWaitingException();
        }
    }

    private void validateDateTimeWaiting(Reservation reservation) {
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new InvalidDateTimeWaitingException();
        }
    }

    public void deleteReservationWaiting(Long id, Member member) {
        ReservationWaiting waiting = findReservationWaitingById(id);
        if (waiting.isNotPublishedBy(member)) {
            throw new CannotDeleteOtherMemberWaiting();
        }
        reservationWaitingRepository.delete(waiting);
    }

    private ReservationWaiting findReservationWaitingById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(NotFoundReservationWaitingException::new);
    }

    private Reservation findReservationByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(NotFoundReservationException::new);
    }
}

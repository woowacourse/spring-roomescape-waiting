package roomescape.service.reservationwaiting;

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
import roomescape.exception.reservationwaiting.NotFoundReservationWaitingException;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;

@Service
@Transactional
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long saveReservationWaiting(ReservationWaitingRequest request, Member member) {
        Reservation reservation = findReservationById(request.getReservationId());
        if (reservationWaitingRepository.existsByReservationAndMember(reservation, member)) {
            throw new DuplicatedReservationWaitingException();
        }
        ReservationWaiting reservationWaiting = new ReservationWaiting(reservation, member);
        ReservationWaiting savedReservationWaiting = reservationWaitingRepository.save(reservationWaiting);
        return savedReservationWaiting.getId();
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

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }
}

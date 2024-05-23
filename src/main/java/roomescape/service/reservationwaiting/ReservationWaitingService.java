package roomescape.service.reservationwaiting;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.reservationwaiting.DuplicatedReservationWaitingException;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;

@Service
@Transactional
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository, EntityManager entityManager) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.entityManager = entityManager;
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

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }
}

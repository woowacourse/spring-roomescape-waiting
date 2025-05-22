package roomescape.reservation.infrastructure;

import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;

@Repository
public class WaitingReservationRepositoryImpl implements WaitingReservationRepository {

    private final JpaWaitingReservationRepository jpaWaitingReservationRepository;

    public WaitingReservationRepositoryImpl(JpaWaitingReservationRepository jpaWaitingReservationRepository) {
        this.jpaWaitingReservationRepository = jpaWaitingReservationRepository;
    }

    @Override
    public WaitingReservation save(final WaitingReservation waitingReservation) {
        return jpaWaitingReservationRepository.save(waitingReservation);
    }

    @Override
    public Integer findMaxWaitingOrderByReservationId(final Long reservationId) {
        return jpaWaitingReservationRepository.findMaxWaitingOrderByReservationId(reservationId);
    }

}

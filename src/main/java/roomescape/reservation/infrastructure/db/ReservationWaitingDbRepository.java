package roomescape.reservation.infrastructure.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.infrastructure.db.dao.ReservationWaitingJpaRepository;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationWaitingRepository;

@Repository
@RequiredArgsConstructor
public class ReservationWaitingDbRepository implements ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;
    @Override
    public void save(ReservationWaiting reservationWaiting) {
        reservationWaitingJpaRepository.save(reservationWaiting);
    }
}

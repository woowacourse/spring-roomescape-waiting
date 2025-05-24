package roomescape.reservation.infrastructure.jpa.waiting;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

@Repository
public class ReservationWaitingImpl implements ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;

    public ReservationWaitingImpl(final ReservationWaitingJpaRepository reservationWaitingJpaRepository) {
        this.reservationWaitingJpaRepository = reservationWaitingJpaRepository;
    }

    @Override
    public boolean existsByReservationIdAndMemberId(final long reservationId, final long memberId) {
        return reservationWaitingJpaRepository.existsByReservationIdAndMemberId(reservationId, memberId);
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
       return reservationWaitingJpaRepository.save(reservationWaiting);
    }

    @Override
    public void deleteById(final long id) {
        reservationWaitingJpaRepository.deleteById(id);
    }

    @Override
    public List<ReservationWaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        return reservationWaitingJpaRepository.findWaitingsWithRankByMemberId(memberId);
    }
}

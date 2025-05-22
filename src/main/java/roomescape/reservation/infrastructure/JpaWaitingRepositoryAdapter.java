package roomescape.reservation.infrastructure;

import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;

import java.util.List;

@Repository
public class JpaWaitingRepositoryAdapter implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public JpaWaitingRepositoryAdapter(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public boolean exists(Long reservationId, Long memberId) {
        return jpaWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public List<WaitingWithRank> findByMemberId(Long memberId) {
        return jpaWaitingRepository.findByMemberId(memberId);
    }
}

package roomescape.waiting.infrastructure;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@Repository
public class JpaWaitingRepositoryAdapter implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public JpaWaitingRepositoryAdapter(final JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(final Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public List<Waiting> findByWaitingMemberId(final Long memberId) {
        return jpaWaitingRepository.findByWaitingsMemberId(memberId);
    }

    @Override
    public void deleteByReservationId(final Long reservationId, final Long memberId) {
        jpaWaitingRepository.deleteByReservationIdAndMemberId(reservationId, memberId);
    }

    @Override
    public boolean existsByReservationIdAndMemberId(final Long reservationId, final Long memberId) {
        return jpaWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);
    }
}

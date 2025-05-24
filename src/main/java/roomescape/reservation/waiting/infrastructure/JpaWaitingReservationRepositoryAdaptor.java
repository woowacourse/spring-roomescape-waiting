package roomescape.reservation.waiting.infrastructure;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

@Repository
public class JpaWaitingReservationRepositoryAdaptor implements WaitingReservationRepository {

    private final JpaWaitingReservationRepository jpaWaitingReservationRepository;

    public JpaWaitingReservationRepositoryAdaptor(JpaWaitingReservationRepository jpaWaitingReservationRepository) {
        this.jpaWaitingReservationRepository = jpaWaitingReservationRepository;
    }

    @Override
    public WaitingReservation save(WaitingReservation waitingReservation) {
        return jpaWaitingReservationRepository.save(waitingReservation);
    }

    @Override
    public List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId) {
        return jpaWaitingReservationRepository.findWaitingsWithRankByMember_Id(memberId);
    }

    @Override
    public void deleteByIdAndMemberId(Long id, Long memberId) {
        jpaWaitingReservationRepository.deleteByIdAndMemberId(id, memberId);
    }

    @Override
    public boolean existsByIdAndMemberId(Long id, Long memberId) {
        return jpaWaitingReservationRepository.existsByIdAndMemberId(id, memberId);
    }
}

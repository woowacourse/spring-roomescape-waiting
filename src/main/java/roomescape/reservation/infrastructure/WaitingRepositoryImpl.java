package roomescape.reservation.infrastructure;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.domain.WaitingWithRank;

@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    @Override
    public Waiting save(final Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public void delete(final Waiting waiting) {
        jpaWaitingRepository.delete(waiting);
    }

    @Override
    public void deleteById(final Long waitingId) {
        jpaWaitingRepository.deleteById(waitingId);
    }

    @Override
    public boolean existsById(final Long waitingId) {
        return jpaWaitingRepository.existsById(waitingId);
    }

    @Override
    public boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member) {
        return jpaWaitingRepository.existsByReservationSlotAndMember(reservationSlot, member);
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<WaitingWithRank> findAllWaitingWithRankByMemberId(final Long memberId) {
        return jpaWaitingRepository.findAllWaitingWithRankByMemberId(memberId);
    }

    @Override
    public List<WaitingWithRank> findAllWaitingWithRank() {
        return jpaWaitingRepository.findAllWaitingWithRank();
    }

    @Override
    public Optional<Waiting> findFirstByReservationSlotOrderByCreatedAt(
            final ReservationSlot reservationSlot) {
        return jpaWaitingRepository.findFirstByReservationSlotOrderByCreatedAt(reservationSlot);
    }
}

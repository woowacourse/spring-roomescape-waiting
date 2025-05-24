package roomescape.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void delete(Waiting waiting);

    void deleteById(Long waitingId);

    boolean existsById(Long waitingId);

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);

    Optional<Waiting> findById(Long waitingId);

    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    List<WaitingWithRank> findAllWaitingWithRank();

    Optional<Waiting> findFirstByReservationSlotOrderByCreatedAt(ReservationSlot reservationSlot);
}

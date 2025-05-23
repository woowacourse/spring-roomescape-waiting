package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    @Query("SELECT new roomescape.waiting.domain.WaitingWithRank( "
            + "w, "
            + "(SELECT COUNT(w2) "
            + "FROM Waiting w2 "
            + "WHERE w2.reservation = w.reservation "
            + "AND w2.createdAt <= w.createdAt)) "
            + "FROM Waiting w "
            + "WHERE w.member.id = :memberId ")
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByReservationIdOrderByCreatedAtAsc(Long id);
}

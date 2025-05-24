package roomescape.reservation.waiting.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.domain.WaitingWithRank;

import java.util.List;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    List<Waiting> findAll();

    @Query("SELECT new roomescape.reservation.waiting.domain.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.createdAt < w.createdAt)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}

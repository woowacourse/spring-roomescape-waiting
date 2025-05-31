package roomescape.reservation.infrastructure.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.WaitingRepository;
import roomescape.reservation.model.vo.WaitingWithRank;

@Repository
public interface WaitingRepositoryImpl extends WaitingRepository,
    JpaRepository<Waiting, Long> {

    @Query("SELECT new roomescape.reservation.model.vo.WaitingWithRank(" +
        "    w, " +
        "    (SELECT COUNT(w2) AS Long" +
        "     FROM roomescape.reservation.model.entity.Waiting w2 " +
        "     WHERE w2.theme = w.theme " +
        "       AND w2.date = w.date " +
        "       AND w2.time = w.time " +
        "       AND w2.id < w.id)) " +
        "FROM Waiting w " +
        "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);
}

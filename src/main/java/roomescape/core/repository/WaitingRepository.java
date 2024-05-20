package roomescape.core.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.core.domain.Member;
import roomescape.core.domain.Waiting;
import roomescape.core.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    @Query("SELECT new roomescape.core.domain.WaitingWithRank(" +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id), " +
            "    w) " +
            "FROM Waiting w " +
            "WHERE w.member = :member")
    List<WaitingWithRank> findAllWithRankByMember(@Param("member") final Member member);
}

package roomescape.waiting.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMember(User member);

    @Query("SELECT new roomescape.waiting.domain.WaitingWithRank(" +
            "    w, " +
            "    CAST((SELECT COUNT(w2) " +
            "          FROM Waiting w2 " +
            "          WHERE w2.theme = w.theme " +
            "            AND w2.date = w.date " +
            "            AND w2.time = w.time " +
            "            AND w2.id < w.id) AS long)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

}

package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Waiting save(Waiting waiting);

    @Query("""
            SELECT new roomescape.presentation.dto.response.WaitingWithRank(
                w.theme.name,
                w.date,
                w.time.startAt,
                (
                    SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w2.theme = w.theme
                      AND w2.date = w.date
                      AND w2.time = w.time
                      AND w2.createdAt < w.createdAt
                )
            )
            FROM Waiting w
            WHERE w.memberId = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);


}

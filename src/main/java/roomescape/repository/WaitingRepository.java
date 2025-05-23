package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.dto.business.WaitingWithRank(
                w, (SELECT COUNT(w2) + 1 FROM Waiting w2 WHERE w2.id < w.id))
            FROM Waiting w
            WHERE w.member.id = :memberId
            ORDER BY w.date
            """)
    List<WaitingWithRank> findWithRankingByMember(long memberId);

    @Query("""
            SELECT EXISTS(
                SELECT w
                FROM Waiting w
                WHERE w.theme.id = :themeId
                AND w.date = :date
                AND w.time.id = :timeId
                AND w.member.id = :memberId
            )
            """)
    boolean existsDuplicated(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date,
            @Param("timeId") long timeId,
            @Param("memberId") long memberId
    );

    @Query("""
            SELECT w
            FROM Waiting w
            WHERE w.theme.id = :themeId
            AND w.date = :date
            AND w.time.id = :timeId
            ORDER BY w.id
            LIMIT 1
            """)
    Optional<Waiting> findFirstWaiting(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date,
            @Param("timeId") long timeId
    );

    boolean existsByTheme(Theme theme);
}

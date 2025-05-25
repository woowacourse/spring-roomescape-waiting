package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.dto.business.WaitingWithRank(
                w, (
                    SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w2.theme.id = w.theme.id
                    AND w2.date = w.date
                    AND w2.time.id = w.time.id
                    AND w2.createdDate < w.createdDate
                )
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            ORDER BY w.createdDate ASC
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
            ORDER BY w.createdDate
            LIMIT 1
            """)
    Optional<Waiting> findFirstWaiting(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date,
            @Param("timeId") long timeId
    );

    boolean existsByTheme(Theme theme);

    boolean existsByTime(ReservationTime reservationTime);
}

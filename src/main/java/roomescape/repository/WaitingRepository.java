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

    @Query(value = """
            SELECT
                w.id AS id,
                w.date AS date,
                t.name AS theme_name,
                rt.start_at AS start_at,
                ROW_NUMBER() OVER (PARTITION BY :memberId ORDER BY w.created_at DESC) + 1 AS rank
            FROM waiting AS w
            LEFT JOIN theme AS t ON w.theme_id = t.id
            LEFT JOIN reservation_time AS rt ON w.time_id = rt.id
            LEFT JOIN member AS m ON w.member_id = m.id
            WHERE w.member_id = :memberId
            """, nativeQuery = true)
    List<WaitingWithRank> findWithRankingByMember(@Param("memberId") long memberId);

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
            ORDER BY w.createdAt
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

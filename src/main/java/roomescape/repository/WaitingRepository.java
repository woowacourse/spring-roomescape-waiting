package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

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
}

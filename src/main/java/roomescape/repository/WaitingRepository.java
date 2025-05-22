package roomescape.repository;

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
                AND w.time.id = :timeId
                AND w.member.id = :memberId
            )
            """)
    boolean existsDuplicated(
            @Param("themeId") long themeId,
            @Param("timeId") long timeId,
            @Param("memberId") long memberId
    );
}

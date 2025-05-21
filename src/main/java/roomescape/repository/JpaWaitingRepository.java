package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.waiting.Waiting;

import java.time.LocalDate;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
    SELECT exists (
        SELECT w
        FROM Waiting w
        WHERE w.date = :date AND w.time.id = :timeId AND w.theme.id = :themeId AND w.member.id = :memberId)
    """)
    boolean existsFor(LocalDate date, Long timeId, Long themeId, Long memberId);
}

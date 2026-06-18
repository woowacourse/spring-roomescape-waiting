package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

import jakarta.persistence.LockModeType;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2) + 1
                 FROM Waiting w2
                 WHERE w2.theme = w.theme
                 AND w2.date = w.date
                 AND w2.time = w.time
                 AND w2.id < w.id)
            )
            FROM Waiting w
            WHERE w.memberName = :memberName
            ORDER BY w.id
            """)
    List<WaitingWithRank> findWithRankByMemberName(@Param("memberName") String memberName);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Waiting> findByMemberNameOrderByIdAsc(String memberName);

    boolean existsByMemberNameAndThemeAndDateAndTime(
            String memberName,
            Theme theme,
            LocalDate date,
            Time time
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Waiting> findFirstByThemeAndDateAndTimeOrderByIdAsc(
            Theme theme,
            LocalDate date,
            Time time
    );
}

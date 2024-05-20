package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationDate;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, ReservationDate date, Long timeId, Long themeId);

    @Query("""
            SELECT new roomescape.domain.WaitingWithRank(
                w,
                CAST((SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w2.theme = w.theme
                      AND w2.date = w.date
                      AND w2.time = w.time
                      AND w2.id < w.id) AS Long))
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    @Query("""
                SELECT w
                FROM Waiting w
                WHERE w.date = :date
                AND w.time.id = :timeId
                AND w.theme.id = :themeId
                ORDER BY w.id
                LIMIT 1
            """)
    Optional<Waiting> findByDateAndTimeIdAndThemeIdOrderById(ReservationDate date, Long timeId, Long themeId);
}

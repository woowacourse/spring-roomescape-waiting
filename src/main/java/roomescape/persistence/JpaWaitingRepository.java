package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;
import roomescape.persistence.dto.WaitingWithRankData;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Waiting w
        WHERE w.bookingInfo.member.id = :memberId
          AND w.bookingInfo.theme.id = :themeId
          AND w.bookingInfo.time.id = :timeId
          AND w.bookingInfo.date = :date
        )
    """)
    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);

    @Query("""
        SELECT w FROM Waiting w
        WHERE w.bookingInfo.theme.id = :themeId
          AND w.bookingInfo.time.id = :timeId
          AND w.bookingInfo.date = :date
        ORDER BY w.id ASC
        LIMIT 1
    """)
    Optional<Waiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.bookingInfo.member
        JOIN FETCH w.bookingInfo.time
        JOIN FETCH w.bookingInfo.theme
        WHERE w.bookingInfo.member.id = :memberId
        ORDER BY w.id
    """)
    List<Waiting> findByMemberId(Long memberId);

    @Query("""
        SELECT new roomescape.persistence.dto.WaitingWithRankData(
            w,
            (
                SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.bookingInfo.date = w.bookingInfo.date
                  AND w2.bookingInfo.time = w.bookingInfo.time
                  AND w2.bookingInfo.theme = w.bookingInfo.theme
                  AND w2.waitingStartedAt < w.waitingStartedAt
            )
        )
        FROM Waiting w
        WHERE w.bookingInfo.member.id = :memberId
        ORDER BY w.waitingStartedAt, w.id
    """)
    List<WaitingWithRankData> findWaitingsWithRankByMemberId(Long memberId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.bookingInfo.member
        JOIN FETCH w.bookingInfo.time
        JOIN FETCH w.bookingInfo.theme
        ORDER BY w.id ASC
    """)
    List<Waiting> findAllWaitings();
}

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
        WHERE w.member.id = :memberId
          AND w.bookingSlot.theme.id = :themeId
          AND w.bookingSlot.time.id = :timeId
          AND w.bookingSlot.date = :date
        )
    """)
    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);

    @Query("""
        SELECT w FROM Waiting w
        WHERE w.bookingSlot.theme.id = :themeId
          AND w.bookingSlot.time.id = :timeId
          AND w.bookingSlot.date = :date
        ORDER BY w.id ASC
        LIMIT 1
    """)
    Optional<Waiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.member
        JOIN FETCH w.bookingSlot.time
        JOIN FETCH w.bookingSlot.theme
        WHERE w.member.id = :memberId
        ORDER BY w.id
    """)
    List<Waiting> findByMemberId(Long memberId);

    @Query("""
        SELECT new roomescape.persistence.dto.WaitingWithRankData(
            w,
            (
                SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.bookingSlot.date = w.bookingSlot.date
                  AND w2.bookingSlot.time = w.bookingSlot.time
                  AND w2.bookingSlot.theme = w.bookingSlot.theme
                  AND (
                       w2.waitingStartedAt < w.waitingStartedAt OR
                       (w2.waitingStartedAt = w.waitingStartedAt AND w2.id < w.id)
                  )
            )
        )
        FROM Waiting w
        WHERE w.member.id = :memberId
        ORDER BY w.waitingStartedAt, w.id
    """)
    List<WaitingWithRankData> findWaitingsWithRankByMemberId(Long memberId);

    @Query("""
        SELECT w FROM Waiting w
        JOIN FETCH w.member
        JOIN FETCH w.bookingSlot.time
        JOIN FETCH w.bookingSlot.theme
        ORDER BY w.id ASC
    """)
    List<Waiting> findAllWaitings();
}

package roomescape.waiting.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("SELECT COUNT(w) > 0 "
        + "FROM Waiting w "
        + "WHERE w.member.id = :memberId "
        + "AND w.reservationSchedule.theme.id = :themeId "
        + "AND w.reservationSchedule.reservationTime.id = :reservationTimeId "
        + "AND w.reservationSchedule.date = :date ")
    boolean existsWaiting(
        Long memberId,
        Long themeId,
        Long reservationTimeId,
        LocalDate date
    );

    @Query("SELECT new roomescape.waiting.domain.WaitingWithRank(" +
        "    w, " +
        "    (SELECT COUNT(w2) + 1" +
        "     FROM Waiting w2 " +
        "     WHERE w2.reservationSchedule.theme.id = w.reservationSchedule.theme.id" +
        "       AND w2.reservationSchedule.date = w.reservationSchedule.date" +
        "       AND w2.reservationSchedule.reservationTime.id = w.reservationSchedule.reservationTime.id " +
        "       AND w2.id < w.id)) " +
        "FROM Waiting w " +
        "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);


    @NativeQuery(value = """
        SELECT *
        FROM waiting w
        WHERE w.theme_id = ?
        AND w.date = ?
        AND w.reservation_time_id = ?
        ORDER BY id
        LIMIT 1
        """)
    Optional<Waiting> findFirstWaiting(Long themeId, LocalDate date, Long reservationTimeId);
}

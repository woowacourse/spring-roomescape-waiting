package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.vo.WaitingWithRank;

@Repository
public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    @Query("""
            SELECT w
            FROM Waiting w
            WHERE w.id = (
                SELECT MIN(w2.id)
                FROM Waiting w2
                WHERE w2.theme.id = :themeId
                    AND w2.date = :date
                    AND w2.reservationTime.startAt = :startAt)
            """)
    Optional<Waiting> findFirstByThemeIdAndDateAndReservationTimeStartAt(Long themeId, LocalDate date, LocalTime startAt);

    @Query("""
            SELECT new roomescape.reservation.vo.WaitingWithRank(
                w,
                (SELECT cast (COUNT(w2) as long)
                FROM Waiting w2
                WHERE w2.theme = w.theme
                    AND w2.date = w.date
                    AND w2.reservationTime = w.reservationTime
                    AND w2.id <= w.id))
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRanksByMemberId(Long memberId);

    boolean existsByThemeIdAndDateAndReservationTimeStartAt(Long themeId, LocalDate date, LocalTime startAt);
}

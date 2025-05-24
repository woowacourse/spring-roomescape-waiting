package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.domain.reservation.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                 FROM Waiting w2
                 WHERE w2.schedule.reservationDate = w.schedule.reservationDate
                   AND w2.schedule.reservationTime = w.schedule.reservationTime
                   AND w2.schedule.theme = w.schedule.theme
                   AND w2.waitStartAt < w.waitStartAt)
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") final Long memberId);

    @Query("""
                SELECT w.id FROM Waiting w
                WHERE w.schedule.reservationDate.date = :date
                  AND w.schedule.reservationTime.id = :timeId
                  AND w.schedule.theme.id = :themeId
                  AND w.member.id = :memberId
            """)
    List<Long> findIdsByScheduleAndMember(
            @Param("date") final LocalDate date,
            @Param("timeId") final Long timeId,
            @Param("themeId") final Long themeId,
            @Param("memberId") final Long memberId,
            Pageable pageable
    );

    default boolean existsByScheduleAndMemberId(
            final LocalDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId
    ) {
        return !findIdsByScheduleAndMember(date, timeId, themeId, memberId, Pageable.ofSize(1)).isEmpty();
    }
}

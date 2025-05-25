package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;


public interface JpaWaitingRepository extends WaitingRepository,
        ListCrudRepository<Waiting, Long> {

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM Waiting w
                WHERE (w.info.date, w.info.time.id, w.info.theme.id) = (:date, :timeId, :themeId)
            )
            """)
    boolean existsByDateAndTimeIdAndThemeId(
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId
    );

    @Query("""
                SELECT COALESCE(MAX(w.turn), 0)
                  FROM Waiting w
                 WHERE w.info.date   = :date
                   AND w.info.time.id  = :timeId
                   AND w.info.theme.id = :themeId
            """)
    int findMaxOrderByDateAndTimeAndTheme(
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId
    );

    @Query("""
              SELECT new roomescape.reservation.dto.response.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                   FROM Waiting w2
                  WHERE w2.info.date = w.info.date
                    AND w2.info.time.id = w.info.time.id
                    AND w2.info.theme.id = w.info.theme.id
                    AND w2.turn <  w.turn
                ) + 1
              )
              FROM Waiting w
              WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") Long memberId);

    Optional<Waiting> findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(
            LocalDate date,
            ReservationTime time,
            Theme theme
    );
}

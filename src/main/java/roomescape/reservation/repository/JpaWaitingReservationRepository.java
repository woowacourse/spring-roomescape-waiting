package roomescape.reservation.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;


public interface JpaWaitingReservationRepository extends WaitingReservationRepository,
        JpaRepository<Waiting, Long> {

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.info.time t
            JOIN FETCH r.info.theme th
            JOIN FETCH r.member m
            WHERE th.id        = :themeId
            AND m.id         = :memberId
            AND r.info.date BETWEEN :startDate AND :endDate
            """)
    List<Waiting> findFilteredReservations(Long themeId,
                                           Long memberId,
                                           LocalDate startDate,
                                           LocalDate endDate);

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
            SELECT w
            FROM Waiting w
            JOIN FETCH w.member m
            WHERE m.id = :memberId
            """)
    List<Waiting> findAllByMemberId(@Param("memberId") Long memberId);

    List<Waiting> findAll();

    void deleteById(Long id);

    @Query("""
              SELECT new roomescape.reservation.dto.response.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                   FROM Waiting w2
                  WHERE w2.info.date       = w.info.date
                    AND w2.info.time.id    = w.info.time.id
                    AND w2.info.theme.id   = w.info.theme.id
                    AND w2.turn            <  w.turn
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

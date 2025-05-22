package roomescape.infrastructure;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.Status;
import roomescape.presentation.dto.response.ReservationWithAhead;

public interface JpaReservationDao extends JpaRepository<Reservation, Id> {

    @Query("""
            SELECT DISTINCT r
              FROM Reservation r
              JOIN FETCH r.time rt
              JOIN FETCH r.theme t
              JOIN FETCH r.user u
             WHERE (:themeId  IS NULL OR t.id    = :themeId)
               AND (:userId   IS NULL OR u.id    = :userId)
               AND (:dateFrom IS NULL OR r.date.value >= :dateFrom)
               AND (:dateTo   IS NULL OR r.date.value <= :dateTo)
               AND r.status = roomescape.business.model.vo.Status.RESERVED
            """)
    List<Reservation> findAllWithFilter(
            @Param("themeId") Id themeId,
            @Param("userId") Id userId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
                SELECT new roomescape.presentation.dto.response.ReservationWithAhead(
                    r,
                    (
                        SELECT COUNT(r2) + 1L
                        FROM Reservation r2
                        WHERE r2.theme      = r.theme
                          AND r2.date       = r.date
                          AND r2.time       = r.time
                          AND r2.createdAt < r.createdAt
                    )
                )
                FROM Reservation r
                WHERE r.user.id = :userId
            """)
    List<ReservationWithAhead> findReservationsWithAhead(@Param("userId") Id userId);

    List<Reservation> findAllByStatus(Status status);

    boolean existsByDateValueAndTimeStartTimeValueAndThemeId(LocalDate date, LocalTime time, Id themeId);

    boolean existsByTimeId(Id timeId);

    boolean existsByThemeId(Id themeId);

    @Transactional
    @Modifying
    @Query("""
              UPDATE Reservation r
                 SET r.status = roomescape.business.model.vo.Status.RESERVED
               WHERE r.date       = :date
                 AND r.time       = :time
                 AND r.theme      = :theme
                 AND r.status     = roomescape.business.model.vo.Status.WAITING
                 AND r.createdAt  = (
                     SELECT MIN(r2.createdAt)
                       FROM Reservation r2
                      WHERE r2.date    = :date
                        AND r2.time    = :time
                        AND r2.theme   = :theme
                        AND r2.status  = roomescape.business.model.vo.Status.WAITING
                 )
            """)
    void updateWaitingReservations(
            @Param("date") ReservationDate date,
            @Param("time") ReservationTime time,
            @Param("theme") Theme theme
    );
}

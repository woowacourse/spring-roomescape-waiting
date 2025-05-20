package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;
import roomescape.presentation.dto.response.ReservationWithAhead;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
                        SELECT COUNT(r2) * 1L
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

    boolean existsByDateValueAndTimeStartTimeValueAndThemeId(LocalDate date, LocalTime time, Id themeId);

    boolean existsByTimeId(Id timeId);

    boolean existsByThemeId(Id themeId);
}

package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;

public interface JpaReservationDao extends JpaRepository<Reservation, Id> {

    @Query("""
            SELECT DISTINCT r
              FROM Reservation r
              JOIN FETCH r.time rt
              JOIN FETCH r.theme t
              JOIN FETCH r.user u
             WHERE (:themeId  IS NULL OR t.id    = :themeId)
               AND (:userId   IS NULL OR u.id    = :userId)
               AND (:dateFrom IS NULL OR r.date >= :dateFrom)
               AND (:dateTo   IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findAllWithFilter(
            @Param("themeId") Id themeId,
            @Param("userId") Id userId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END 
              FROM Reservation r
             WHERE r.date      = :date
               AND r.time.startTime = :time
               AND r.theme.id  = :themeId
            """)
    boolean existsByDateAndTimeAndTheme(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("themeId") Id themeId
    );

    boolean existByTimeId(Id timeId);

    boolean existByThemeId(Id themeId);
}

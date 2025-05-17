package roomescape.infrastructure.Jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.StartTime;

import java.util.List;

public interface JpaReservationTimeDao extends JpaRepository<ReservationTime, Id> {

    @Query("""
            SELECT rt
            FROM ReservationTime rt
            WHERE rt.id NOT IN (
                 SELECT r.time.id
                   FROM Reservation r
                  WHERE r.date = :date
                    AND r.theme.id = :themeId
            )
            """)
    List<ReservationTime> findAvailableByDateAndThemeId(
            @Param("date") ReservationDate date,
            @Param("themeId") Id themeId
    );

    @Query("""
            SELECT rt
            FROM ReservationTime rt
            WHERE rt.id IN (
                 SELECT r.time.id
                   FROM Reservation r
                  WHERE r.date = :date
                    AND r.theme.id = :themeId
            )
            """)
    List<ReservationTime> findNotAvailableByDateAndThemeId(
            @Param("date") ReservationDate date,
            @Param("themeId") Id themeId
    );

    boolean existsByStartTimeBetween(StartTime startTime, StartTime endTime);

    boolean existsByStartTime(StartTime startTime);
}

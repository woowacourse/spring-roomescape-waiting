package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query(
        value = """
               SELECT
                 rt.id,
                 rt.start_at,
                 EXISTS(
                   SELECT r.id
                   FROM reservation as r
                   WHERE r.time_id = rt.id
                       AND r.theme_id = :themeId
                       AND r.date = :date
                 ) as already_booked
               FROM reservation_time as rt
               ORDER BY rt.start_at ASC;
            """, nativeQuery = true)
    List<ReservationTime> findAllTimesWithBooked(@Param("date") LocalDate date,
        @Param("themeId") long themeId);

    boolean existsByStartAt(LocalTime startAt);
}

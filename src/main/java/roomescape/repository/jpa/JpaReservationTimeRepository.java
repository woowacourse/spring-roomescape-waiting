package roomescape.repository.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.entity.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

public interface JpaReservationTimeRepository extends ReservationTimeRepository,
    CrudRepository<ReservationTime, Long> {

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
        @Param("themeId") Long themeId);

    boolean existsByStartAt(LocalTime startAt);
}

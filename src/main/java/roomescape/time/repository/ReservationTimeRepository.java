package roomescape.time.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.time.domain.ReservableTime;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime reservationTime);

    @Query(value = """
            SELECT t.id AS time_id, t.start_at AS start_at, 
                EXISTS(
                    SELECT 1
                    FROM reservation r
                    WHERE r.time_id = t.id AND r.date = :date AND r.theme_id = :themeId
                ) AS is_booked
            FROM reservation_time AS t;
            """, nativeQuery = true)
    List<ReservableTime> findAllReservableTime(
            @Param("date") LocalDate date,
            @Param("themeId") long themeId
    );
}
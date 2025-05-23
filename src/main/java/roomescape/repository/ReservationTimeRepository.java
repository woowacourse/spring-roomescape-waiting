package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT new roomescape.domain.time.AvailableReservationTime(
                rt,
                CASE WHEN COUNT(r.id) > 0 THEN true ELSE false END
            )
            FROM ReservationTime rt
            LEFT JOIN ReservationSchedule rs 
                ON rs.reservationTime = rt AND rs.reservationDate.date = :date AND rs.theme.id = :themeId
            LEFT JOIN Reservation r 
                ON r.schedule = rs
            GROUP BY rt
            """)
    List<AvailableReservationTime> findAllAvailableReservationTimes(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );

    boolean existsByStartAt(LocalTime startAt);
}

package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query(value = """
            SELECT reservation_time.id, reservation_time.start_at FROM reservation_time 
            INNER JOIN reservation as re 
            ON re.time_id = reservation_time.id 
            INNER JOIN member_reservation as mr 
            ON re.id = mr.reservation_id 
            WHERE re.date = ? AND re.theme_id = ?;
            """, nativeQuery = true)
    Set<ReservationTime> findReservedTime(LocalDate date, long themeId);
}

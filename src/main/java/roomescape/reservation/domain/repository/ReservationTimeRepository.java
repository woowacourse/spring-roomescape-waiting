package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query("""
                SELECT rt FROM Reservation r
                JOIN r.reservationSlot rs 
                JOIN rs.time rt 
                WHERE rs.date = :date AND rs.theme.id = :themeId
            """)
    Set<ReservationTime> findReservedTime(LocalDate date, long themeId);
}

package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    @Query("""
            SELECT rt FROM ReservationTime rt
            WHERE rt.id NOT IN (
                SELECT s.time.id FROM Reservation r
                JOIN r.slot s
                WHERE s.date.value = :date AND s.theme.id = :themeId
            )
            """)
    List<ReservationTime> findAvailableByDateAndTheme(LocalDate date, long themeId);
}

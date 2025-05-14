package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.entity.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    @Query(value = """
            SELECT rt
            FROM ReservationTime rt
            WHERE rt.id IN (
                SELECT r.time.id
                FROM Reservation r
                WHERE r.date = :date AND r.theme.id = :themeId
            )
            """)
    List<ReservationTime> findAllReservedTimeByDateAndThemeId(LocalDate date, Long themeId);
}

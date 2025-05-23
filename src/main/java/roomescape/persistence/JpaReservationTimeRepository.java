package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;
import roomescape.persistence.dto.ReservationTimeAvailabilityData;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT new roomescape.persistence.dto.ReservationTimeAvailabilityData(
                rt.id, rt.startAt,
                EXISTS (
                    SELECT 1 FROM Reservation r
                    WHERE r.bookingSlot.time = rt AND r.bookingSlot.theme.id = :themeId AND r.bookingSlot.date = :date
                )
            )
            FROM ReservationTime rt
            ORDER BY rt.startAt
            """)
    List<ReservationTimeAvailabilityData> findAvailableTimesByThemeAndDate(
            @Param("themeId") Long themeId,
            @Param("date") LocalDate date);
}

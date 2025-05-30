package roomescape.reservationtime.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalDate;
import java.util.List;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
                SELECT rt FROM ReservationTime rt
                WHERE rt.id NOT IN (
                    SELECT r.schedule.time.id
                    FROM Reservation r
                    WHERE r.schedule.date = :date AND r.schedule.theme.id = :themeId
                )
            """)
    List<ReservationTime> findAllByReservationDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") Long themeId);
}

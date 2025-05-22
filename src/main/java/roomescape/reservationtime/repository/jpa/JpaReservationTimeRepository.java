package roomescape.reservationtime.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservationtime.domain.ReservationTime;

public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT rt FROM ReservationTime rt
            WHERE rt.id NOT IN (
                SELECT r.time.id
                FROM Reservation r
                WHERE r.date = :date AND r.theme.id = :themeId
            )
        """)
    List<ReservationTime> findAllByReservationDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") Long themeId);
}

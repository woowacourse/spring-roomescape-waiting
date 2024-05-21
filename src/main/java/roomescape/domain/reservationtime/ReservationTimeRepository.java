package roomescape.domain.reservationtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT
                  new roomescape.domain.reservationtime.AvailableReservationTimeDto(
                  rt.id,
                  rt.startAt,
                  CASE WHEN COUNT(r.id) > 0 THEN true ELSE false END
                  )
            FROM ReservationTime rt
            LEFT JOIN Reservation r
            ON r.time.id = rt.id AND r.date = :date AND r.theme.id = :themeId
            GROUP BY rt.id, rt.startAt
            """)
    List<AvailableReservationTimeDto> findAvailableReservationTimes(LocalDate date, long themeId);

    boolean existsByStartAt(LocalTime startAt);
}

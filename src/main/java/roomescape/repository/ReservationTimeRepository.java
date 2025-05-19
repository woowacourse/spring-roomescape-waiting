package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT rt
            FROM ReservationTime AS rt
            ORDER BY rt.startAt ASC
            """)
    List<ReservationTime> findAllOrderByStartAt();

    @Query("""
            SELECT rt
            FROM ReservationTime AS rt
            WHERE EXISTS (
                SELECT r
                FROM Reservation AS r
                WHERE r.reservationTime.id = rt.id
                AND r.theme.id = :themeId
                AND r.date = :date
            )
            """)
    List<ReservationTime> findReservationTimesWithBookState(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date
    );

    boolean existsByStartAt(LocalTime startAt);
}

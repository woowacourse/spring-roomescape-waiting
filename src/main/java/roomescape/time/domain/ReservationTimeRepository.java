package roomescape.time.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            SELECT r.time
            FROM Reservation AS r
            WHERE r.date.value = :date
                AND r.theme.id = :themeId
            """)
    List<ReservationTime> findReservedTime(@Param("date") LocalDate date, @Param("themeId") Long themeId);

    boolean existsByStartAt(LocalTime startAt);
}

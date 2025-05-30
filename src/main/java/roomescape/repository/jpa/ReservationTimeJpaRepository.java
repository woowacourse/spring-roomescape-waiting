package roomescape.repository.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(final LocalTime startAt);

    @Query("""
        SELECT rt 
        FROM ReservationTime rt 
        LEFT JOIN Reservation r ON rt.id = r.time.id 
            AND r.date = :date 
            AND r.theme.id = :themeId
        WHERE r.id IS NULL
        """)
    List<ReservationTime> findAvailableTimesByDateAndThemeId(
            @Param("date") final LocalDate date,
            @Param("themeId") final long themeId
    );
}

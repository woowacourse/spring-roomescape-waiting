package roomescape.reservationtime.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservationtime.domain.ReservationTime;

interface SpringDataReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

    @Query("""
            SELECT rt
            FROM Slot s
            JOIN s.time rt
            WHERE s.date = :date
              AND s.theme.id = :themeId
            """)
    List<ReservationTime> findTimesByDateAndThemeId(
            @Param("date") LocalDate date,
            @Param("themeId") long themeId
    );
}

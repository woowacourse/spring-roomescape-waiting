package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime localTime);

    @Query("""
            SELECT r.time
            FROM Reservation r
            INNER JOIN ReservationTime t ON r.time = t
            INNER JOIN Theme th ON r.theme = th
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<ReservationTime> findByDateAndThemeId(final LocalDate date, final Long themeId);
}

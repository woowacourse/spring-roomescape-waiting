package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;
import roomescape.dto.projection.ReservationTimeStatusProjection;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    @Query("""
            SELECT new roomescape.dto.projection.ReservationTimeStatusProjection(
                t,
                CASE WHEN EXISTS (
                    SELECT 1 FROM Reservation r
                    WHERE r.time = t AND r.theme.id = :themeId AND r.date = :date
                ) THEN roomescape.domain.ReservationStatus.CONFIRMED
                  ELSE roomescape.domain.ReservationStatus.AVAILABLE END
            )
            FROM ReservationTime t
            ORDER BY t.startAt
            """)
    List<ReservationTimeStatusProjection> findAvailableTime(Long themeId, LocalDate date);

    ReservationTime findReservationTimeById(Long id);
}

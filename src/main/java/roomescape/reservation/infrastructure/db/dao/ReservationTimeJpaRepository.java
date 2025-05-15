package roomescape.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.entity.ReservationTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    @Query(value = """
                            SELECT rt.id, rt.start_at
                            FROM reservation r
                            INNER JOIN reservation_time rt ON r.time_id = rt.id
                            WHERE r.theme_id = :themeId AND r.date = :date
            """, nativeQuery = true)
    List<ReservationTime> findByThemeIdAndDate(Long themeId, LocalDate date);
}

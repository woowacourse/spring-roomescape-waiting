package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    // 과도기: reservation 이 아직 엔티티가 아니라 JPQL 불가 -> native.
    // 1-2에서 Reservation 엔티티화되면 JPQL(NOT EXISTS/안티조인)로 승격 예정.
    @Query(value = """
            SELECT id, start_at
            FROM reservation_time
            WHERE id NOT IN (
                SELECT time_id FROM reservation WHERE date = :date AND theme_id = :themeId
            )
            """, nativeQuery = true)
    List<ReservationTime> findAvailable(@Param("date") LocalDate date, @Param("themeId") Long themeId);
}

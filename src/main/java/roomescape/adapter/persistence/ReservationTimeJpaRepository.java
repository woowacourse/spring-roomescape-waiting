package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;

public interface ReservationTimeJpaRepository extends JpaRepository<ReservationTime, Long> {

    // Reservation 이 엔티티가 되어 native -> JPQL 안티조인으로 승격.
    @Query("""
            select rt from ReservationTime rt
            where rt.id not in (
                select r.time.id from Reservation r
                where r.date = :date and r.theme.id = :themeId
            )
            """)
    List<ReservationTime> findAvailable(@Param("date") LocalDate date, @Param("themeId") Long themeId);
}

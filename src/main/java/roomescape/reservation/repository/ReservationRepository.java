package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAll();

    List<Reservation> findAllByTimeId(Long id);

    @Query(value = """
            SELECT 
                r.id,
                r.date,
                r.time_id,
                rt.start_at as time_value,
                r.theme_id, 
                r.member_id 
            FROM reservation r 
            LEFT JOIN reservation_time rt ON r.time_id = rt.id 
            LEFT JOIN theme t ON r.theme_id = t.id 
            LEFT JOIN member m ON r.member_id = m.id
            WHERE r.theme_id = :themeId
            AND r.member_id = :memberId
            AND r.date >= :dateFrom
            AND r.date <= :dateTo
            ORDER BY r.date, rt.start_at
            """, nativeQuery = true)
    List<Reservation> findAllFiltered(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByDateAndTimeId(LocalDate date, Long timeId);
}

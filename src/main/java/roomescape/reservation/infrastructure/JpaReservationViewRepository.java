package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationView;

import java.util.List;
import java.util.Optional;

public interface JpaReservationViewRepository extends JpaRepository<ReservationView, String> {

    boolean existsByDateAndTimeIdAndThemeIdAndUserId(ReservationDate date, Long timeId, Long themeId, Long userId);

    List<ReservationView> findAllByUserId(Long userId);

    @Query(value = """
            SELECT wr.id FROM reservations r 
            JOIN waiting_reservations wr ON (wr.date = r.date AND wr.time_id = r.time_id AND wr.theme_id = r.theme_id) 
            WHERE r.id = :id 
            ORDER BY wr.waiting_order ASC 
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> findFirstWaitingByReservationId(Long id);
}


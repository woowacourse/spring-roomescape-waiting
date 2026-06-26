package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByStartAt(LocalTime startAt);

    @Query(value = """
            SELECT t.id, t.start_at,
                   CASE WHEN r.id IS NULL THEN TRUE ELSE FALSE END AS available
            FROM time_slot t
            LEFT JOIN session s ON t.id = s.time_id AND s.theme_id = :themeId AND s.date = :date
            LEFT JOIN reservation r ON s.id = r.session_id
            """, nativeQuery = true)
    List<AvailableTimeSlotView> findAvailableSlotViews(@Param("themeId") long themeId, @Param("date") LocalDate date);

    interface AvailableTimeSlotView {
        Long getId();
        LocalTime getStartAt();
        boolean getAvailable();
    }
}

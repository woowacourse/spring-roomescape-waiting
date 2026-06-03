package roomescape.support.datasource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ReservationDataSource extends BaseDataSource {

    public boolean hasReservationById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }

    public void insertReservation(
            String name,
            LocalDate date,
            Long themeId,
            Long timeId,
            String status,
            LocalDateTime createdAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, date, theme_id, time_id, status, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                name, date, themeId, timeId, status, createdAt);
    }
}

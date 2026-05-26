package roomescape.repository;

import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationWaitingQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationWaitingQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isExistByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, Long timeId, Long themeId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1 
                    FROM reservation 
                    WHERE name = ? 
                    AND date = ?
                    AND time_id = ?
                    AND theme_id = ?
            )
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, name, date, timeId, themeId));
    }
}

package roomescape.reservationtime.infra;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.application.dto.AvailableReservationTimeResult;
import roomescape.reservationtime.application.dao.AvailableTimeDao;

@Repository
public class JdbcAvailableTimeDao implements AvailableTimeDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAvailableTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AvailableReservationTimeResult> findByThemeAndDate(Long themeId, LocalDate date) {
        String sql = """
                SELECT rt.id, rt.start_at,
                    NOT EXISTS (
                        SELECT 1
                        FROM reservation r
                        WHERE r.time_id = rt.id
                            AND r.theme_id = ?
                            AND r.date = ?
                    ) AS available
                FROM reservation_time rt
                ORDER BY rt.start_at ASC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new AvailableReservationTimeResult(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getBoolean("available")
                ),
                themeId, date);
    }
}

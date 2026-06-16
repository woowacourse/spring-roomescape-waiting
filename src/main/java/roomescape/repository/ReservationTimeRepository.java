package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

@Repository
public class ReservationTimeRepository {
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER =
            (rs, rowNum) -> RepositoryRowMapper.reservationTimeRowMapper(rs);

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationTime> findByDateAndTheme(LocalDate date, long themeId) {
        String sql = """
                SELECT rt.id AS time_id, rt.start_at
                FROM reservation_time AS rt
                WHERE rt.id NOT IN (
                    SELECT s.time_id
                    FROM slot s
                    INNER JOIN reservation r ON r.slot_id = s.id
                    WHERE s.date = ? AND s.theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, date, themeId);
    }
}

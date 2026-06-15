package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.UserReservation;

@Repository
public class UserReservationRepository {
    private final JdbcTemplate jdbctemplate;

    public UserReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbctemplate = jdbcTemplate;
    }

    public List<UserReservation> findByName(String name, int page, int size) {
        String sql = """
                SELECT r.id, r.name, r.date,
                       rt.id AS time_id, rt.start_at AS time_start_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.thumbnail,
                       'RESERVED' AS status, NULL AS rank
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.name = ?
                
                UNION ALL
                
                SELECT w.id, w.name, w.date,
                       rt.id AS time_id, rt.start_at AS time_start_at,
                       t.id AS theme_id, t.name AS theme_name, t.description AS theme_description, t.thumbnail,
                       'WAITING' AS status,
                       (SELECT COUNT(*) FROM waiting w2
                        WHERE w2.theme_id = w.theme_id
                          AND w2.date = w.date
                          AND w2.time_id = w.time_id
                          AND w2.id <= w.id) AS rank
                FROM waiting w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme t ON w.theme_id = t.id
                WHERE w.name = ?
                
                ORDER BY date, time_id, theme_id, status
                LIMIT ?
                OFFSET ?
                """;
        int offset = Math.max(page, 0) * size;
        return jdbctemplate.query(sql, reservationRowsMapper(), name, name, size, offset);
    }

    private RowMapper<UserReservation> reservationRowsMapper() {
        return (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getObject("time_start_at", LocalTime.class)
            );

            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("thumbnail")
            );

            ReservationStatus status = ReservationStatus.valueOf(rs.getString("status"));
            Long rank = rs.getObject("rank", Long.class);

            UserReservation userReservation = new UserReservation(
                    rs.getLong("id"),
                    rs.getString("name"),
                    LocalDate.parse(rs.getString("date")),
                    time,
                    theme,
                    status,
                    rank);

            return userReservation;
        };
    }
}

package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Time;
import roomescape.domain.Theme;
import roomescape.dto.ReservationResponse;
import roomescape.dto.WaitingResponse;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getDate("date").toLocalDate(),
            new Time(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))
    );

    private final RowMapper<ReservationResponse> reservationResponseRowMapper = (rs, rowNum) -> ReservationResponse.from(
            new Reservation(
            rs.getLong("id"),
            rs.getDate("date").toLocalDate(),
            new Time(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))),
            new WaitingResponse(rs.getLong("waiting_id"), rs.getString("waiting_name"), rs.getInt("waiting_order"))
            );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(LocalDate date, Long timeId, Long themeId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
        )).longValue();
    }

    public Reservation findById(Long id) {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                where r.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    public List<ReservationResponse> findByUserName(String username) {
        String sql = """
                SELECT r.id, 
                       r.date,
                       t.id AS time_id, 
                       t.start_at AS time_value,
                       th.id AS theme_id, 
                       th.name AS theme_name, 
                       th.description AS theme_description, 
                       th.thumbnail_url AS theme_thumbnail,
                      w.id AS waiting_id,
                      w.name AS waiting_name,
                      (
                          SELECT COUNT(*)
                          FROM waiting w2
                          WHERE w2.reservation_id = w.reservation_id
                            AND w2.id < w.id
                      ) AS waiting_order
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN waiting AS w ON r.id = w.reservation_id
                WHERE w.name = ?
                """;
        return jdbcTemplate.query(sql, reservationResponseRowMapper, username);
    }

    public void updateDateAndTimeById(long id, LocalDate date, long timeId) {
        jdbcTemplate.update("UPDATE reservation SET date = ?, time_id = ? WHERE id = ?", date, timeId, id);
    }

    public Optional<Reservation> findByDateAndTimeId(LocalDate date, Long timeId) {
        String sql = """
            SELECT r.id AS reservation_id,
                   r.date AS date,
                   rt.id AS time_id,
                   rt.start_at AS start_at,
                   t.id AS theme_id,
                   t.name AS theme_name,
                   t.description AS description,
                   t.thumbnail_url AS thumbnail_url
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            WHERE r.date = ? AND r.time_id = ?
            """;
        return jdbcTemplate.query(sql, reservationRowMapper, date, timeId)
                .stream()
                .findFirst();
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}

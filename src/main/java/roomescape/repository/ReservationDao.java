package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Time;
import roomescape.domain.Theme;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            new Time(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
            new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))
    );

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(String name, LocalDate date, Long timeId, Long themeId) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
        )).longValue();
    }

    public Reservation findById(Long id) {
        String sql = """
                SELECT r.id, 
                       r.name, 
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
                       r.name, 
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

    public List<Reservation> findByUserName(String username) {
        String sql = """
                SELECT r.id, 
                       r.name, 
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
                WHERE r.name = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, username);
    }

    public void updateDateAndTimeById(long id, LocalDate date, long timeId) {
        jdbcTemplate.update("UPDATE reservation SET date = ?, time_id = ? WHERE id = ?", date, timeId, id);
    }


    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}

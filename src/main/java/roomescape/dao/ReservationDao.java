package roomescape.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class ReservationDao {

    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
        );

        return new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservation.getName());
        parameters.put("date", reservation.getDate());
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        return reservation.createWithId(generatedId.longValue());
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id, 
                       r.name as reservation_name, 
                       r.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS rt 
                ON r.time_id = rt.id
                INNER JOIN theme AS t 
                ON r.theme_id = t.id
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<Reservation> findAllByName(String name) {
        String sql = """
                SELECT r.id, 
                       r.name as reservation_name, 
                       r.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS rt 
                ON r.time_id = rt.id
                INNER JOIN theme AS t 
                ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.date DESC, rt.start_at DESC
                ;
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, name);
    }

    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT r.id, 
                       r.name as reservation_name, 
                       r.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS rt 
                    ON r.time_id = rt.id
                INNER JOIN theme AS t 
                    ON r.theme_id = t.id
                WHERE r.id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public boolean existsByReservationTime(long reservationTimeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, reservationTimeId));
    }

    public boolean existsByTheme(long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, themeId));
    }

    public boolean existsByThemeAndDateAndTime(long themeId, LocalDate date, long reservationTimeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                        AND date = ? 
                        AND time_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, themeId, date, reservationTimeId));
    }

    public boolean existsByThemeAndDateAndTimeAndIdNot(long themeId, LocalDate date,
                                                       long reservationTimeId, long reservationId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                      AND date = ?
                      AND time_id = ?
                      AND id != ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date, reservationTimeId, reservationId);
    }

    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?,
                    date = ?,
                    time_id = ?,
                    theme_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, reservation.getName(), reservation.getDate(),
                reservation.getTime().getId(), reservation.getTheme().getId(), reservation.getId());
    }

    public int delete(long reservationId) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, reservationId);
    }
}

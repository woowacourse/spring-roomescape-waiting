package roomescape.reservation.dao;

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
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

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

    public Reservation insert(Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reservation.getName());
        parameters.put("date", reservation.getDate());
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(
                generatedId.longValue(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    public List<Reservation> select() {
        return jdbcTemplate.query(baseSelectSql(), ROW_MAPPER);
    }

    public Optional<Reservation> selectById(long reservationId) {
        try {
            String sql = baseSelectSql() + " WHERE r.id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Reservation> selectByThemeIdAndDate(long themeId, LocalDate date) {
        String sql = baseSelectSql() + " WHERE r.theme_id = ? AND r.date = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, themeId, date);
    }

    public boolean existsByTimeId(long timeId) {
        String sql = "SELECT COUNT(*) > 0 FROM reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT COUNT(*) > 0 FROM reservation WHERE theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?""";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    public boolean existsDuplicateExcluding(LocalDate date, long timeId, long themeId, long reservationId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND id != ?""";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, reservationId);
    }

    public Reservation update(Long reservationId, LocalDate date, long timeId) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, date, timeId, reservationId);
        return selectById(reservationId).get();
    }

    public int delete(Long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, reservationId);
    }

    private String baseSelectSql() {
        return """
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
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                """;
    }
}

package roomescape.repository;

import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public class WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public WaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Waiting save(Waiting waiting) {
        String sql = "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waiting.getName());
            ps.setObject(2, waiting.getDate());
            ps.setObject(3, waiting.getTime().getId());
            ps.setObject(4, waiting.getTheme().getId());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new Waiting(
                id,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme());
    }

    public boolean existsByScheduleAndName(LocalDate date, long timeId, long themeId, String name) {
        String sql = """
                SELECT count(*)
                FROM waiting
                WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId, name);
        return count != null && count > 0;
    }

    public Optional<Waiting> findById(long id) {
        String sql = """
                SELECT w.id          AS waiting_id,
                       w.name        AS waiting_name,
                       w.date        AS waiting_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail  AS theme_thumbnail
                FROM waiting w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.id = ?
                """;
        try {
            Waiting waiting = jdbcTemplate.queryForObject(sql, reservationRowsMapper(), id);
            return Optional.ofNullable(waiting);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Waiting> findByName(String name) {
        String sql = """
                SELECT w.id          AS waiting_id,
                       w.name        AS waiting_name,
                       w.date        AS waiting_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail  AS theme_thumbnail
                FROM waiting w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.name = ?
                ORDER BY w.id
                """;
        return jdbcTemplate.query(sql, reservationRowsMapper(), name);
    }

    public Long countByThemeIdAndDateAndTimeIdAndIdLessThanEqual(Long id, Theme theme, LocalDate date,
                                                                 ReservationTime time) {
        String sql = """
                SELECT COUNT(*) FROM waiting
                WHERE theme_id = ?
                  AND date = ?
                  AND time_id = ?
                  AND id <= ?
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                theme.getId(),
                date,
                time.getId(),
                id);
    }

    public void delete(Waiting waiting) {
        String sql = "DELETE FROM waiting WHERE id = ?";
        jdbcTemplate.update(sql, waiting.getId());
    }

    private RowMapper<Waiting> reservationRowsMapper() {
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

            return new Waiting(
                    rs.getLong("waiting_id"),
                    rs.getString("waiting_name"),
                    LocalDate.parse(rs.getString("waiting_date")),
                    time,
                    theme
            );
        };
    }

    public Optional<Waiting> findFirstBySchedule(LocalDate date, long timeId, long themeId) {
        String sql = """
                SELECT w.id          AS waiting_id,
                       w.name        AS waiting_name,
                       w.date        AS waiting_date,
                       t.id          AS time_id,
                       t.start_at    AS time_start_at,
                       th.id         AS theme_id,
                       th.name       AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail  AS theme_thumbnail
                FROM waiting w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.id
                LIMIT 1
                """;
        try {
            Waiting waiting = jdbcTemplate.queryForObject(sql, reservationRowsMapper(), date, timeId, themeId);
            return Optional.ofNullable(waiting);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

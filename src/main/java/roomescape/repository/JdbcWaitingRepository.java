package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Waiting save(Waiting waiting) {
        String sql = "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waiting.getName());
            ps.setObject(2, waiting.getSchedule().getDate());
            ps.setObject(3, waiting.getSchedule().getTime().getId());
            ps.setObject(4, waiting.getSchedule().getTheme().getId());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new Waiting(
                id,
                waiting.getName(),
                waiting.getSchedule());
    }

    @Override
    public Optional<Waiting> findByScheduleAndName(Waiting waiting) {
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
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ? AND w.name = ?
                ORDER BY w.id
                LIMIT 1
                """;
        try {
            Waiting found = jdbcTemplate.queryForObject(sql, reservationRowsMapper(),
                    waiting.getSchedule().getDate(),
                    waiting.getSchedule().getTime().getId(),
                    waiting.getSchedule().getTheme().getId(),
                    waiting.getName());
            return Optional.ofNullable(found);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
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

    @Override
    public List<Waiting> findUserWaitingList(String name, int page, int size) {
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
                ORDER BY w.date, t.start_at, w.id
                LIMIT ? OFFSET ?
                """;
        int offset = Math.max(page, 0) * size;
        return jdbcTemplate.query(sql, reservationRowsMapper(), name, size, offset);
    }

    @Override
    public Optional<Waiting> findFirstWaitingByScheduleForUpdate(Schedule schedule) {
        String sql = """
                SELECT w.id AS waiting_id,
                       w.name AS waiting_name,
                       w.date AS waiting_date,
                       t.id AS time_id,
                       t.start_at AS time_start_at,
                       th.id AS theme_id,
                       th.name AS theme_name,
                       th.description AS theme_description,
                        th.thumbnail AS theme_thumbnail
                FROM waiting w
                JOIN reservation_time t ON w.time_id = t.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.id
                LIMIT 1
                FOR UPDATE
                """;
        try {
            Waiting waiting = jdbcTemplate.queryForObject(sql, reservationRowsMapper(),
                    schedule.getDate(),
                    schedule.getTime().getId(),
                    schedule.getTheme().getId());
            return Optional.ofNullable(waiting);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long findWaitingOrder(Waiting waiting) {
        String sql = """
            SELECT COUNT(*) + 1 FROM waiting
            WHERE theme_id = ?
              AND date = ?
              AND time_id = ?
              AND id < ?
            """;
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                waiting.getSchedule().getTheme().getId(),
                waiting.getSchedule().getDate(),
                waiting.getSchedule().getTime().getId(),
                waiting.getId());
    }

    @Override
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
                    new Schedule(LocalDate.parse(rs.getString("waiting_date")), time, theme)
            );
        };
    }
}

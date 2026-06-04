package roomescape.repository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation save(Reservation reservation) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        long waitingId = simpleJdbcInsert.executeAndReturnKey(param).longValue();
        return new Reservation(
                waitingId,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    @Override
    public boolean hasWaitingOnSlot(String name, LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS
                (
                    SELECT 1 FROM waiting WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?
                )
                """;
        Boolean hasWaiting = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                name, date, timeId, themeId
        );
        return Boolean.TRUE.equals(hasWaiting);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    w.id AS reservation_id,
                    w.name AS reservation_name,
                    w.date AS reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM waiting w
                INNER JOIN reservation_time t ON w.time_id = t.id
                INNER JOIN theme th ON w.theme_id = th.id
                WHERE w.id = ?
                """;
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    rs.getDate("reservation_date").toLocalDate(),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime()
                    ),
                    new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail")
                    )
            ), id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findFirstWaiting(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT
                    w.id AS reservation_id,
                    w.name AS reservation_name,
                    w.date AS reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM waiting w
                INNER JOIN reservation_time t ON w.time_id = t.id
                INNER JOIN theme th ON w.theme_id = th.id
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.id ASC
                LIMIT 1
                """;
        try {
            Reservation first = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    rs.getDate("reservation_date").toLocalDate(),
                    new ReservationTime(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime()
                    ),
                    new Theme(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail")
                    )
            ), date, timeId, themeId);
            return Optional.ofNullable(first);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    @Override
    public long countWaitingsBefore(Reservation targetWaiting) {
        String sql = """
                SELECT COUNT(*) AS waitingCount
                FROM waiting
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                  AND id < ?
                """;
        Long waitingCount = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                targetWaiting.getDate(), targetWaiting.getTime().getId(), targetWaiting.getTheme().getId(),
                targetWaiting.getId()
        );
        return Objects.requireNonNull(waitingCount, "COUNT 쿼리의 결과는 null일 수 없습니다.");
    }
}

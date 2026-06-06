package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.*;


import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationWaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationWaiting> waitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("time_value", LocalTime.class));
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"));
        ReservationSlot slot = new ReservationSlot(
                resultSet.getObject("date", LocalDate.class), time, theme
        );

        return new ReservationWaiting(
                resultSet.getLong("reservation_waiting_id"),
                resultSet.getString("username"),
                slot
        );
    };

    private final RowMapper<WaitingWithTurn> waitingWithTurnRowMapper = (resultSet, rowNum) -> {
        ReservationWaiting waiting = waitingRowMapper.mapRow(resultSet, rowNum);
        return new WaitingWithTurn(
                waiting,
                resultSet.getLong("turn")
        );
    };

    public ReservationWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<WaitingWithTurn> findByNameWithTurn(String name) {
        String sql = """
                SELECT
                    r.id AS reservation_waiting_id,
                    r.name AS username,
                    r.date,
                    rt.id AS time_id,
                    rt.start_at AS time_value,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail,
                    (
                        SELECT COUNT(*)
                        FROM reservation_waiting AS earlier
                        WHERE earlier.date = r.date
                          AND earlier.time_id = r.time_id
                          AND earlier.theme_id = r.theme_id
                          AND (
                              earlier.created_at < r.created_at
                              OR (
                                  earlier.created_at = r.created_at
                                  AND earlier.id < r.id
                              )
                          )
                    ) + 1 AS turn
                FROM reservation_waiting AS r
                INNER JOIN reservation_time AS rt
                    ON r.time_id = rt.id
                INNER JOIN theme AS t
                    ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.id;
                """;
        return jdbcTemplate.query(sql, waitingWithTurnRowMapper, name);
    }

    public List<WaitingWithTurn> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    r.id AS reservation_waiting_id,
                    r.name AS username,
                    r.date,
                    rt.id AS time_id,
                    rt.start_at AS time_value,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail,
                    (
                        SELECT COUNT(*)
                        FROM reservation_waiting AS earlier
                        WHERE earlier.date = r.date
                          AND earlier.time_id = r.time_id
                          AND earlier.theme_id = r.theme_id
                          AND (
                              earlier.created_at < r.created_at
                              OR (
                                  earlier.created_at = r.created_at
                                  AND earlier.id < r.id
                              )
                          )
                    ) + 1 AS turn
                FROM reservation_waiting AS r
                JOIN reservation_time AS rt ON r.time_id = rt.id
                JOIN theme AS t ON r.theme_id = t.id
                WHERE r.date BETWEEN ? AND ?;
                """;
        return jdbcTemplate.query(sql, waitingWithTurnRowMapper, startDate, endDate);
    }

    public ReservationWaiting insert(ReservationWaiting waiting) {
        ReservationSlot slot = waiting.getSlot();
        String sql = "INSERT INTO reservation_waiting(name, date, time_id, theme_id) VALUES (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(
                    sql,
                    new String[]{"id"});
            pstmt.setString(1, waiting.getName());
            pstmt.setObject(2, slot.getDate());
            pstmt.setLong(3, slot.getTime().getId());
            pstmt.setLong(4, slot.getTheme().getId());
            return pstmt;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return waiting.withId(id);
    }

    public boolean existsByNameAndSlot(String name, ReservationSlot slot) {
        String sql = """
                SELECT
                    count(*)
                FROM
                    reservation_waiting
                WHERE name = ?
                  AND date = ?
                  AND time_id = ?
                  AND theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql,
                Integer.class, name,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId()
        );
        return count != null && count > 0;
    }

    public Optional<ReservationWaiting> findById(Long id) {
        String sql = """
                SELECT
                    r.id as reservation_waiting_id,
                    r.name as username,
                    r.date,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation_waiting as r
                INNER JOIN reservation_time as rt
                  ON r.time_id = rt.id
                INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE r.id = ?;
                """;
        List<ReservationWaiting> result = jdbcTemplate.query(sql, waitingRowMapper, id);
        return result.stream().findAny();
    }

    public Optional<WaitingWithTurn> findByIdWithTurn(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_waiting_id,
                    r.name AS username,
                    r.date,
                    rt.id AS time_id,
                    rt.start_at AS time_value,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail,
                    (
                        SELECT COUNT(*)
                        FROM reservation_waiting AS earlier
                        WHERE earlier.date = r.date
                          AND earlier.time_id = r.time_id
                          AND earlier.theme_id = r.theme_id
                          AND (
                              earlier.created_at < r.created_at
                              OR (
                                  earlier.created_at = r.created_at
                                  AND earlier.id < r.id
                              )
                          )
                    ) + 1 AS turn
                FROM reservation_waiting AS r
                INNER JOIN reservation_time AS rt
                    ON r.time_id = rt.id
                INNER JOIN theme AS t
                    ON r.theme_id = t.id
                WHERE r.id = ?;
                """;
        List<WaitingWithTurn> result = jdbcTemplate.query(sql, waitingWithTurnRowMapper, id);
        return result.stream().findAny();
    }

    public int delete(Long id) {
        String sql = "DELETE FROM reservation_waiting WHERE id = ?;";
        return jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationWaiting> findFirstBySlotForUpdate(ReservationSlot slot) {
        String sql = """
                SELECT
                    r.id AS reservation_waiting_id,
                    r.name AS username,
                    r.date,
                    rt.id AS time_id,
                    rt.start_at AS time_value,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation_waiting AS r
                INNER JOIN reservation_time AS rt
                    ON r.time_id = rt.id
                INNER JOIN theme AS t
                    ON r.theme_id = t.id
                WHERE r.date = ?
                  AND r.time_id = ?
                  AND r.theme_id = ?
                ORDER BY r.created_at ASC, r.id ASC
                LIMIT 1
                FOR UPDATE;
                """;
        List<ReservationWaiting> result = jdbcTemplate.query(
                sql,
                waitingRowMapper,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId()
        );
        return result.stream().findFirst();
    }
}

package roomescape.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.*;
import roomescape.service.dto.ReservationInfoResult;

@Repository
public class ReservationDao {

    private static final ReservationStatus INACTIVE_STATUS = ReservationStatus.CANCELED;

    private static final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_start_at").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        Schedule schedule = new Schedule(
                rs.getLong("schedule_id"),
                theme,
                rs.getDate("schedule_date").toLocalDate(),
                time
        );

        return new Reservation(
                rs.getLong("id"),
                new Reserver(rs.getString("name")),
                schedule,
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("updated_at", LocalDateTime.class)
        );
    };

    private static final RowMapper<ReservationInfoResult> reservationInfoResultRowMapper = (rs, rowNum) ->
            new ReservationInfoResult(
                    reservationRowMapper.mapRow(rs, rowNum),
                    rs.getInt("waiting_order")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(Reservation reservation) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "name", reservation.getReserver().getName(),
                "schedule_id", reservation.getSchedule().getId(),
                "status", reservation.getStatus().name(),
                "updated_at", reservation.getUpdateAt()
        )).longValue();
    }

    public void update(Reservation reservation) {
        jdbcTemplate.update(
                """
                UPDATE reservation
                SET schedule_id = ?,
                    status = ?,
                    updated_at = ?
                WHERE id = ?
                """,
                reservation.getSchedule().getId(),
                reservation.getStatus().name(),
                reservation.getUpdateAt(),
                reservation.getId()
        );
    }

    public void changeStatusWithUpdateAt(Reservation reservation) {
        jdbcTemplate.update(
                "UPDATE reservation SET status = ?, updated_at = ? WHERE id = ?",
                reservation.getStatus().name(), reservation.getUpdateAt(), reservation.getId()
        );
    }

    public void changeStatusOnly(Long reservationId, ReservationStatus status) {
        jdbcTemplate.update(
                "UPDATE reservation SET status = ? WHERE id = ?",
                status.name(), reservationId
        );
    }

    public Optional<Reservation> findById(long id) {
        String sql = """
                SELECT r.id,
                    r.name,
                    r.status,
                    r.updated_at,
                    s.id   AS schedule_id,
                    s.date AS schedule_date,
                    t.id         AS time_id,
                    t.start_at   AS time_start_at,
                    th.id          AS theme_id,
                    th.name        AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN schedule         AS s  ON r.schedule_id = s.id
                INNER JOIN reservation_time AS t  ON s.time_id     = t.id
                INNER JOIN theme            AS th ON s.theme_id    = th.id
                WHERE r.id = ?
                """;

        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, id);
        return results.stream().findFirst();
    }

    public Optional<Reservation> findFirstByScheduleIdAndStatus(long scheduleId, ReservationStatus status) {
        String sql = """
            SELECT r.id,
                   r.name,
                   r.status,
                   r.updated_at,
                   s.id             AS schedule_id,
                   s.date           AS schedule_date,
                   t.id             AS time_id,
                   t.start_at       AS time_start_at,
                   th.id            AS theme_id,
                   th.name          AS theme_name,
                   th.description   AS theme_description,
                   th.thumbnail_url AS theme_thumbnail
            FROM reservation AS r
            INNER JOIN schedule         AS s  ON r.schedule_id = s.id
            INNER JOIN reservation_time AS t  ON s.time_id     = t.id
            INNER JOIN theme            AS th ON s.theme_id    = th.id
            WHERE r.schedule_id = ?
                AND status      = ?
            ORDER BY updated_at ASC
            LIMIT 1
            """;

        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, scheduleId, status.name());
        return results.stream().findFirst();
    }

    public List<ReservationInfoResult> findByName(String name) {
        String sql = """
            WITH active_orders AS (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY schedule_id
                           ORDER BY updated_at ASC, id ASC
                       ) - 1 AS waiting_order
                FROM reservation
                WHERE status != ?
            )
            SELECT r.id,
                   r.name,
                   r.status,
                   r.updated_at,
                   s.id             AS schedule_id,
                   s.date           AS schedule_date,
                   t.id             AS time_id,
                   t.start_at       AS time_start_at,
                   th.id            AS theme_id,
                   th.name          AS theme_name,
                   th.description   AS theme_description,
                   th.thumbnail_url AS theme_thumbnail,
                   COALESCE(ao.waiting_order, 0) AS waiting_order
            FROM reservation AS r
            INNER JOIN schedule         AS s  ON r.schedule_id = s.id
            INNER JOIN reservation_time AS t  ON s.time_id     = t.id
            INNER JOIN theme            AS th ON s.theme_id    = th.id
            LEFT JOIN active_orders     AS ao ON r.id          = ao.id
            WHERE r.name = ?
            ORDER BY s.date, t.start_at, r.updated_at
            """;

        return jdbcTemplate.query(sql, reservationInfoResultRowMapper, INACTIVE_STATUS.name(), name);
    }

    public List<ReservationInfoResult> findAll() {
        String sql = """
            WITH active_orders AS (
                SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY schedule_id
                           ORDER BY updated_at ASC, id ASC
                       ) - 1 AS waiting_order
                FROM reservation
                WHERE status != ?
            )
            SELECT r.id,
                   r.name,
                   r.status,
                   r.updated_at,
                   s.id             AS schedule_id,
                   s.date           AS schedule_date,
                   t.id             AS time_id,
                   t.start_at       AS time_start_at,
                   th.id            AS theme_id,
                   th.name          AS theme_name,
                   th.description   AS theme_description,
                   th.thumbnail_url AS theme_thumbnail,
                   COALESCE(ao.waiting_order, 0) AS waiting_order
            FROM reservation AS r
            INNER JOIN schedule         AS s  ON r.schedule_id = s.id
            INNER JOIN reservation_time AS t  ON s.time_id     = t.id
            INNER JOIN theme            AS th ON s.theme_id    = th.id
            LEFT JOIN active_orders     AS ao ON r.id          = ao.id
            ORDER BY r.id
            """;

        return jdbcTemplate.query(sql, reservationInfoResultRowMapper, INACTIVE_STATUS.name());
    }

    public int countReservationByScheduleId(long scheduleId) {
        String sql = """
            SELECT COUNT(*)
            FROM reservation
            WHERE schedule_id = ?
              AND status     != ?
            """;

        return jdbcTemplate.queryForObject(sql, Integer.class, scheduleId, INACTIVE_STATUS.name());
    }

    public boolean existByNameAndScheduleId(String name, Long scheduleId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE name    = ?
                      AND schedule_id = ?
                      AND status     != ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name, scheduleId, INACTIVE_STATUS.name());
    }
}

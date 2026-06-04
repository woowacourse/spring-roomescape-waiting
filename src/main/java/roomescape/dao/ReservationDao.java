package roomescape.dao;

import static roomescape.dao.rowmapper.ReservationMapper.RESERVATION_ROW_MAPPER;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.EventSlot;

@Repository
public class ReservationDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", reservation.getName().value());
        params.put("date", reservation.getEventSlot().date());
        params.put("time_id", reservation.getEventSlot().time().getId());
        params.put("theme_id", reservation.getEventSlot().theme().getId());
        params.put("status", reservation.getStatus().name());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return Reservation.restore(
                id,
                reservation.getName(),
                reservation.getEventSlot(),
                reservation.getStatus()
        );
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status,
                    rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        RESERVATION_ROW_MAPPER,
                        id
                ).stream()
                .findFirst();
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(
                """
                            SELECT r.id, r.name, r.date, r.status,
                            rt.id AS time_id, rt.start_at,
                            t.id AS theme_id, t.name AS theme_name, t.description, t.url
                            FROM reservation r
                            INNER JOIN reservation_time rt ON r.time_id = rt.id
                            INNER JOIN theme t ON r.theme_id = t.id;
                        """,
                RESERVATION_ROW_MAPPER
        );
    }

    public List<Reservation> findByUserName(String userName) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status,
                    rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.name = ? AND r.status = 'CONFIRMED';
                """;
        return jdbcTemplate.query(
                sql,
                RESERVATION_ROW_MAPPER,
                userName
        );
    }

    public List<Reservation> findByAfterDateTime(LocalDateTime now) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status,
                    rt.id AS time_id, rt.start_at,
                    t.id AS theme_id, t.name AS theme_name, t.description, t.url
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.date >= ? AND rt.start_at > ?;
                """;
        return jdbcTemplate.query(
                sql,
                RESERVATION_ROW_MAPPER,
                now.toLocalDate(),
                now.toLocalTime()
        );
    }

    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ? AND status IN ('CONFIRMED', 'PENDING')
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                timeId
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ? AND status IN ('CONFIRMED', 'PENDING')
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                themeId
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsBySlot(EventSlot eventSlot) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation
                            WHERE date = ?
                                AND time_id = ?
                                AND theme_id = ?
                                AND status IN ('CONFIRMED', 'PENDING')
                        ) 
                        """,
                Boolean.class,
                eventSlot.date(),
                eventSlot.time().getId(),
                eventSlot.theme().getId()
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByUserNameAndSlot(String userName, EventSlot eventSlot) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM reservation
                    WHERE name = ?
                        AND date = ?
                        AND time_id = ?
                        AND theme_id = ?
                        AND status IN ('CONFIRMED', 'PENDING')
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                userName,
                eventSlot.date(),
                eventSlot.time().getId(),
                eventSlot.theme().getId()
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?, date = ?, time_id = ?, theme_id = ?, status = ?
                WHERE id = ?;
                """;

        int affectedRows = jdbcTemplate.update(
                sql,
                reservation.getName().value(),
                reservation.getEventSlot().date(),
                reservation.getEventSlot().time().getId(),
                reservation.getEventSlot().theme().getId(),
                reservation.getStatus().name(),
                reservation.getId()
        );

        return affectedRows > 0;
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}

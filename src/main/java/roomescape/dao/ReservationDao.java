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
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
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

        Slot slot = new Slot(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );

        return new Reservation(
                resultSet.getLong("id"),
                slot,
                resultSet.getString("reservation_name"),
                ReservationStatus.valueOf(resultSet.getString("status"))
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
        parameters.put("slot_id", reservation.getSlot().getId());
        parameters.put("status", reservation.getStatus().name());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return reservation.createWithId(generatedId.longValue());
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id,
                       r.name as reservation_name,
                       r.status,
                       s.id as slot_id,
                       s.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<Reservation> findAllByName(String name) {
        String sql = """
                SELECT r.id,
                       r.name as reservation_name,
                       r.status,
                       s.id as slot_id,
                       s.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE r.name = ?
                ORDER BY s.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, name);
    }

    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT r.id,
                       r.name as reservation_name,
                       r.status,
                       s.id as slot_id,
                       s.date,
                       rt.id as time_id,
                       rt.start_at,
                       t.id as theme_id,
                       t.name as theme_name,
                       t.description,
                       t.thumbnail
                FROM reservation AS r
                INNER JOIN slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE r.id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateStatus(long reservationId, ReservationStatus status) {
        String sql = """
                UPDATE reservation
                SET status = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, status.name(), reservationId);
    }

    public boolean existsBySlotIdForUpdate(long slotId) {
        String sql = """
                SELECT id
                FROM reservation
                WHERE slot_id = ?
                FOR UPDATE
                """;
        List<Integer> result = jdbcTemplate.queryForList(sql, Integer.class, slotId);
        return !result.isEmpty();
    }

    public boolean existsByReservationTime(long reservationTimeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation AS r
                    INNER JOIN slot AS s ON r.slot_id = s.id
                    WHERE s.time_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, reservationTimeId));
    }

    public boolean existsByTheme(long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation AS r
                    INNER JOIN slot AS s ON r.slot_id = s.id
                    WHERE s.theme_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, themeId));
    }

    public boolean existsByThemeAndDateAndTime(long themeId, LocalDate date, long reservationTimeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation AS r
                    INNER JOIN slot AS s ON r.slot_id = s.id
                    WHERE s.theme_id = ?
                      AND s.date = ?
                      AND s.time_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, themeId, date, reservationTimeId));
    }

    public boolean existsByThemeAndDateAndTimeAndIdNot(long themeId, LocalDate date,
                                                       long reservationTimeId, long reservationId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation AS r
                    INNER JOIN slot AS s ON r.slot_id = s.id
                    WHERE s.theme_id = ?
                      AND s.date = ?
                      AND s.time_id = ?
                      AND r.id != ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date, reservationTimeId, reservationId);
    }

    public boolean existsBySlotIdAndName(long slotId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation r
                    INNER JOIN slot s ON r.slot_id = s.id
                    WHERE s.id = ?
                      AND r.name = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, boolean.class, slotId, name));
    }

    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?,
                    slot_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, reservation.getName(), reservation.getSlot().getId(), reservation.getId());
    }

    public int delete(long reservationId) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, reservationId);
    }
}

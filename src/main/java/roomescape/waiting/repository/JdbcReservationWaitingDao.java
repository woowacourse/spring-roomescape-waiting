package roomescape.waiting.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

@Repository
public class JdbcReservationWaitingDao implements ReservationWaitingDao {

    private static final RowMapper<ReservationWaiting> RESERVATION_WAITING_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        ReservationSlot slot = new ReservationSlot(
                resultSet.getDate("reservation_waiting_date").toLocalDate(),
                time,
                theme
        );

        return new ReservationWaiting(
                resultSet.getLong("reservation_waiting_id"),
                resultSet.getString("reservation_waiting_name"),
                slot,
                resultSet.getTimestamp("reservation_waiting_updated_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        String sql = """
                INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationWaiting.getName());
            ps.setDate(2, Date.valueOf(reservationWaiting.getDate()));
            ps.setLong(3, reservationWaiting.getTime().getId());
            ps.setLong(4, reservationWaiting.getTheme().getId());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(reservationWaiting.getRequestedAt()));
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        return new ReservationWaiting(
                id,
                reservationWaiting.getName(),
                reservationWaiting.getSlot(),
                reservationWaiting.getRequestedAt()
        );
    }

    @Override
    public Optional<ReservationWaiting> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at AS reservation_waiting_updated_at
                FROM reservation_waiting r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<ReservationWaiting> findAllByName(String name) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at AS reservation_waiting_updated_at
                FROM reservation_waiting r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.name = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, name);
    }

    @Override
    public boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_waiting
                    WHERE reservation_date = ? AND time_id = ? AND name = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, name);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void delete(ReservationWaiting reservationWaiting) {
        String sql = """
                DELETE FROM reservation_waiting
                WHERE id = ?
                """;

        int affected = jdbcTemplate.update(sql, reservationWaiting.getId());
        if (affected == 0) {
            throw new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND);
        }
    }

    @Override
    public List<ReservationWaiting> findAllByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId,
                                                                              Long themeId) {
        String sql = """
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at AS reservation_waiting_updated_at
                FROM reservation_waiting r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme h ON r.theme_id = h.id
                WHERE r.reservation_date = ? AND r.time_id = ? AND r.theme_id = ?
                ORDER BY r.id ASC
                FOR UPDATE
                """;

        return jdbcTemplate.query(sql, RESERVATION_WAITING_ROW_MAPPER, date, timeId, themeId);
    }

    @Override
    public List<ReservationWaiting> findAllBySlots(List<ReservationSlot> slots) {
        if (slots.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT r.id AS reservation_waiting_id,
                       r.name AS reservation_waiting_name,
                       r.reservation_date AS reservation_waiting_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at AS reservation_waiting_updated_at
                FROM reservation_waiting r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme h ON r.theme_id = h.id
                WHERE 
                """);

        List<Object> params = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("(r.reservation_date = ? AND r.time_id = ? AND r.theme_id = ?)");
            ReservationSlot slot = slots.get(i);
            params.add(slot.date());
            params.add(slot.time().getId());
            params.add(slot.theme().getId());
        }

        return jdbcTemplate.query(sql.toString(), RESERVATION_WAITING_ROW_MAPPER, params.toArray());
    }
}

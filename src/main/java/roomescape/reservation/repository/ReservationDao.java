package roomescape.reservation.repository;

import static java.sql.Timestamp.valueOf;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class ReservationDao {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
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
                resultSet.getDate("reservation_date").toLocalDate(),
                time,
                theme
        );

        return new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                slot,
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                resultSet.getBoolean("confirmed")
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Reservation save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (name, reservation_date, time_id, theme_id, updated_at, confirmed)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setTimestamp(5, valueOf(reservation.getUpdatedAt()));
            ps.setBoolean(6, reservation.isConfirmed());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        return new Reservation(
                id,
                reservation.getName(),
                new ReservationSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme()),
                reservation.getUpdatedAt(),
                reservation.isConfirmed()
        );
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name AS reservation_name,
                       r.reservation_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at,
                       r.confirmed
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    public List<Reservation> findAllByName(String name) {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name AS reservation_name,
                       r.reservation_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at,
                       r.confirmed
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.name = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, name);
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name AS reservation_name,
                       r.reservation_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at,
                       r.confirmed
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT r.id AS reservation_id,
                       r.name AS reservation_name,
                       r.reservation_date,
                       r.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       r.updated_at,
                       r.confirmed
                FROM reservation r
                INNER JOIN reservation_time t
                  ON r.time_id = t.id
                INNER JOIN theme h
                  ON r.theme_id = h.id
                WHERE r.reservation_date = ? AND r.time_id = ? AND r.theme_id = ?
                """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, Date.valueOf(date), timeId, themeId)
                .stream().findFirst();
    }

    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET name = ?, reservation_date = ?, time_id = ?, theme_id = ?, updated_at = ?, confirmed = ?
                WHERE id = ?
                """;

        int affected = jdbcTemplate.update(
                sql,
                reservation.getName(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                valueOf(reservation.getUpdatedAt()),
                reservation.isConfirmed(),
                reservation.getId()
        );
        if (affected == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    public void delete(Reservation reservation) {
        String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;
        int affected = jdbcTemplate.update(sql, reservation.getId());
        if (affected == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    public boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE reservation_date = ? AND time_id = ? AND name = ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, name);
        return Boolean.TRUE.equals(exists);
    }

    public boolean existsByDateAndTimeIdAndNameAndIdNot(LocalDate date, Long timeId, String name, Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE reservation_date = ? AND time_id = ? AND name = ? AND id != ?
                )
                """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, name, id);
        return Boolean.TRUE.equals(exists);
    }
}

package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.deleted_at AS reservation_deleted_at,
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.id = ? AND r.deleted_at IS NULL
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.deleted_at AS reservation_deleted_at,
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.deleted_at IS NULL
                ORDER BY r.id
                LIMIT ? OFFSET ?
                """, reservationRowMapper, size, (page - 1) * size);
    }

    @Override
    public List<Reservation> findByGuestName(String guestName) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.deleted_at AS reservation_deleted_at,
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.guest_name = ? AND r.deleted_at IS NULL
                """, reservationRowMapper, guestName);
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation (guest_name, date, time_id, theme_id)
                            VALUES (?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservation.getGuestName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());
            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return reservation.withId(id);
    }

    @Override
    public boolean updateDateAndTime(Long id, LocalDate date, Long timeId) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ? AND deleted_at IS NULL
                """;

        int count = jdbcTemplate.update(sql,
                date,
                timeId,
                id);

        return count == 1;
    }

    @Override
    public boolean cancelById(Long id) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET deleted_at = ?, delete_token = ?
                WHERE id = ? AND deleted_at IS NULL
                """, LocalDateTime.now(clock), id, id);

        return rowCount == 1;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND deleted_at IS NULL
                """, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndIdNot(
            LocalDate date, Long timeId, Long themeId, Long id) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND id != ? AND deleted_at IS NULL
                """, Integer.class, date, timeId, themeId, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE time_id = ? AND deleted_at IS NULL
                """, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = ? AND deleted_at IS NULL
                """, Integer.class, themeId);
        return count != null && count > 0;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime(),
                toLocalDateTime(resultSet.getTimestamp("time_deleted_at"))
        );

        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail"),
                toLocalDateTime(resultSet.getTimestamp("theme_deleted_at"))
        );

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("guest_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                toLocalDateTime(resultSet.getTimestamp("reservation_deleted_at"))
        );
    };

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}

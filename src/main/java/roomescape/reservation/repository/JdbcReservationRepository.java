package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
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
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationWaitingDto> findWaitingById(Long id) {
        return jdbcTemplate.query("""
            SELECT *
            FROM (
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,

                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,

                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at,

                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, t.id, th.id, r.status
                        ORDER BY r.id
                    ) AS wait_number

                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
            ) x
            WHERE x.reservation_id = ?
            """,
                reservationWaitingDtoRowMapper,
                id
        ).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
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
                ORDER BY r.id
                LIMIT ? OFFSET ?
                """, reservationRowMapper, size, (page - 1) * size);
    }

    @Override
    public List<ReservationWaitingDto> findAllByGuestName(String guestName) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,

                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,

                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, t.id, th.id, r.status
                        ORDER BY r.id
                    ) AS wait_number
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.guest_name = ?
                """, reservationWaitingDtoRowMapper, guestName);
    }

    @Override
    public List<ReservationWaitingDto> findAllByGuestNameExceptCanceled(String guestName) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,

                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,

                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, t.id, th.id, r.status
                        ORDER BY r.id
                    ) AS wait_number
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.guest_name = ? AND r.status != 'CANCELED'
                """, reservationWaitingDtoRowMapper, guestName);
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation (guest_name, date, time_id, theme_id, status)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservation.getGuestName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());
            preparedStatement.setString(5, reservation.getStatus().toString());
            return preparedStatement;
        }, keyHolder);

        return reservation.withId(keyHolder.getKey().longValue());
    }

    @Override
    public boolean updateDateAndTime(Long id, LocalDate date, Long timeId, Status status) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?, status = ?
                WHERE id = ?
                """;

        int count = jdbcTemplate.update(sql,
                date,
                timeId,
                status.toString(),
                id);

        return count == 1;
    }

    @Override
    public boolean cancelById(Long id) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET delete_token = ?, status = 'CANCELED'
                WHERE id = ?
                """, id, id);

        return rowCount == 1;
    }


    @Override
    public boolean updateStatus(Long id, Status status) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET status = ?
                WHERE id = ?
                """, status.toString(), id);
        return rowCount == 1;
    }

    @Override
    public Optional<Long> findFirstWaitingIdBySlot(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.query("""
                SELECT id
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND status = 'WAITING'
                ORDER BY id
                LIMIT 1
                """, (rs, rowNum) -> rs.getLong("id"), date, timeId, themeId).stream().findFirst();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndGuestNameExceptCanceled(
            LocalDate date, Long timeId, Long themeId, String guestName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND guest_name = ? AND status != 'CANCELED'
                """, Integer.class, date, timeId, themeId, guestName);
        return count != null && count > 0;
    }

    @Override
    public boolean existsReservationBySlot(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ? AND status = 'CONFIRMED';
                """, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE time_id = ? AND status != 'CANCELED'
                """, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = ? AND status != 'CANCELED'
                """, Integer.class, themeId);
        return count != null && count > 0;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
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
                Status.from(resultSet.getString("status"))
        );
    };

    private final RowMapper<ReservationWaitingDto> reservationWaitingDtoRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
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

        return ReservationWaitingDto.from(Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("guest_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                Status.from(resultSet.getString("status"))),
                resultSet.getLong("wait_number")
        );
    };

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}

package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.dto.ReservationTimesWithStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public Reservation save(final Reservation newReservation) {
        try {
            final long newReservationId = insertReservation(newReservation);
            return newReservation.withId(newReservationId);
        } catch (final DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }
    }

    public void update(final Reservation reservation) {
        final String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getId()
        );
    }

    public void updateStatusById(final Long reservationId, final ReservationStatus reservationStatus) {
        final String sql = """
                UPDATE reservation
                SET status = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                reservationStatus.name(),
                reservationId
        );
    }

    public boolean deleteById(final Long reservationId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, reservationId) > 0;
    }

    public List<Reservation> findAll() {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id 
                ORDER BY r.id
                """;

        return jdbcTemplate.query(sql, ReservationRepository::mapToDomain)
                .stream()
                .toList();
    }

    public List<Reservation> findByName(final String name) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id 
                WHERE r.name = ?
                ORDER BY r.id
                """;

        return jdbcTemplate.query(sql, ReservationRepository::mapToDomain, name)
                .stream()
                .toList();
    }

    public Optional<Reservation> findById(final Long reservationId) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.status AS reservation_status,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    t.end_at AS time_end_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id 
                WHERE r.id = ?
                """;

        try {
            final Reservation reservation = jdbcTemplate.queryForObject(
                    sql,
                    ReservationRepository::mapToDomain,
                    reservationId
            );

            return Optional.of(reservation);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsById(final Long id) {
        final String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE id = ?
                """;

        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        return count != null && count > 0;
    }

    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;

        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);

        return count != null && count > 0;
    }

    public boolean existsByTimeId(final Long timeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE time_id = ?
                """;

        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);

        return count != null && count > 0;
    }

    public boolean existsByThemeId(final Long themeId) {
        final String sql = """
                SELECT COUNT(id)
                FROM reservation
                WHERE theme_id = ?
                """;

        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);

        return count != null && count > 0;
    }

    public List<ReservationTimesWithStatus> findReservationTimeStatusesByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    CASE
                        WHEN r.id IS NOT NULL OR w.id IS NOT NULL THEN TRUE
                        ELSE FALSE
                    END AS reserved
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON r.time_id = rt.id
                   AND r.date = ?
                   AND r.theme_id = ?
                LEFT JOIN waiting_list w
                    ON w.time_id = rt.id
                   AND w.date = ?
                   AND w.theme_id = ?
                ORDER BY rt.start_at;
                """;

        return jdbcTemplate.query(
                        sql,
                        ReservationRepository::mapToTimesWithStatus,
                        date,
                        themeId,
                        date,
                        themeId
                ).stream().toList();
    }

    private long insertReservation(final Reservation reservation) {
        final String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, reservation.getName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());
            preparedStatement.setString(5, reservation.getStatus().name());

            return preparedStatement;
        }, keyHolder);

        return JdbcUtil.extractGeneratedKey(keyHolder);
    }

    /**
     * ResultSet - Domain 매핑 메서드
     */
    private static Reservation mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        final ReservationTime reservationTime = ReservationTime.createWithId(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime(),
                resultSet.getTime("time_end_at").toLocalTime()
        );

        final Theme theme = Theme.createWithId(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url"),
                resultSet.getLong("theme_price")
        );

        return Reservation.from(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme,
                ReservationStatus.valueOf(resultSet.getString("reservation_status"))
        );
    }

    /**
     * ResultSet - DTO 매핑 메서드
     */
    private static ReservationTimesWithStatus mapToTimesWithStatus(final ResultSet resultSet, final int rowNum) throws SQLException {
        return new ReservationTimesWithStatus(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime(),
                resultSet.getBoolean("reserved")
        );
    }
}

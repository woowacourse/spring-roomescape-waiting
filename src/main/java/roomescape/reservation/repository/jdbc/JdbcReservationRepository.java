package roomescape.reservation.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Profile("jdbc")
@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                ORDER BY r.id
                """;

        return jdbcTemplate.query(sql, this::mapToDomain)
                .stream()
                .toList();
    }

    @Override
    public List<Reservation> findAllByCustomerNameAndReservationDateTimeAfter(
        final CustomerName customerName,
        final LocalDateTime now
    ) {
        final String sql = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
                r.date AS reservation_date,
                r.theme_id AS theme_id,
                t.id AS time_id,
                t.start_at AS time_start_at,
                h.name AS theme_name,
                h.description AS theme_description,
                h.thumbnail_url AS theme_thumbnail_url
            FROM reservation r
            JOIN reservation_time t ON r.time_id = t.id
            JOIN theme h ON r.theme_id = h.id
            WHERE r.name = ?
              AND (r.date > ? OR (r.date = ? AND t.start_at > ?))
            ORDER BY r.date ASC
            """;

        return jdbcTemplate.query(
                sql,
                this::mapToDomain,
                customerName.name(),
                now,
                Date.valueOf(now.toLocalDate()),
                Time.valueOf(now.toLocalTime()))
            .stream()
            .toList();
    }

    @Override
    public boolean existsBySlot(final LocalDate date, final long reservationTimeId, final long themeId) {
        final String sql = """
            SELECT COUNT(1)
            FROM reservation
            WHERE date = ? AND time_id = ? AND theme_id = ?
            """;
        final Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            Date.valueOf(date),
            reservationTimeId,
            themeId
        );

        return count != null && count != 0;
    }

    @Override
    public Optional<Reservation> findById(final Long reservationId) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                WHERE r.id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, this::mapToDomain, reservationId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Reservation save(final Reservation newReservation) {
        final long newReservationId = insertReservation(newReservation);

        return Reservation.of(
                newReservationId,
                newReservation.getCustomerName(),
                newReservation.getDate(),
                newReservation.getTime(),
                newReservation.getTheme()
        );
    }

    @Override
    public boolean update(final Reservation reservation) {
        final String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?
                """;

        final int updatedCount = jdbcTemplate.update(
                sql,
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getId()
        );

        return hasUpdatedReservation(updatedCount);
    }

    private static boolean hasUpdatedReservation(final int updatedCount) {
        return updatedCount > 0;
    }

    @Override
    public boolean deleteById(final Long reservationId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, reservationId) > 0;
    }

    @Override
    public List<ReservationTimesWithStatus> findReservationTimeStatusesByDateAndThemeId(final LocalDate date, final Long themeId) {
        final String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    CASE
                        WHEN r.id IS NOT NULL THEN TRUE
                        ELSE FALSE
                    END AS reserved
                FROM reservation_time rt
                LEFT JOIN reservation r
                    ON r.time_id = rt.id
                   AND r.date = ?
                   AND r.theme_id = ?
                ORDER BY rt.start_at;
                """;

        return jdbcTemplate.query(
                        sql,
                        this::mapToTimesWithStatus,
                        date,
                        themeId
                ).stream()
                .toList();
    }


    @Override
    public Optional<Reservation> findBySlot(final LocalDate date, final long timeId, final long themeId) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.name AS reservation_name,
                    r.date AS reservation_date,
                    r.theme_id AS theme_id,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme h ON r.theme_id = h.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                FOR UPDATE
                """;
        return jdbcTemplate.query(sql, this::mapToDomain, Date.valueOf(date), timeId, themeId)
            .stream()
            .findFirst();
    }

    private long insertReservation(final Reservation reservation) {
        final String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, reservation.getCustomerName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성된 id를 가져오지 못했습니다.");
        }
        return keyHolder.getKey().longValue();
    }

    private Reservation mapToDomain(
            final ResultSet resultSet,
            final int rowNum
    ) throws SQLException {
        final ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        final Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );
    }

    private ReservationTimesWithStatus mapToTimesWithStatus(final ResultSet resultSet, final int rowNum)
            throws SQLException {
        return new ReservationTimesWithStatus(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime(),
                resultSet.getBoolean("reserved")
        );
    }
}

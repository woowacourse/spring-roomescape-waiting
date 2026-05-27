package roomescape.reservation.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.repository.entity.ReservationEntity;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public List<Reservation> findAllByCustomerNameAndReservationDateTimeAfter(CustomerName customerName, LocalDateTime now) {
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

        return jdbcTemplate.query(sql,
                        this::mapToDomain,
                        customerName.name(),
                        now,
                        Date.valueOf(now.toLocalDate()),
                        Time.valueOf(now.toLocalTime())
                )
                .stream()
                .toList();
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
        final ReservationEntity reservationEntity = toEntity(newReservation);

        final long newReservationId = insertReservation(reservationEntity);

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


    private long insertReservation(final ReservationEntity reservationEntity) {
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

            preparedStatement.setString(1, reservationEntity.name());
            preparedStatement.setDate(2, reservationEntity.date());
            preparedStatement.setLong(3, reservationEntity.timeId());
            preparedStatement.setLong(4, reservationEntity.themeId());

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

    private ReservationEntity toEntity(final Reservation reservation) {
        return new ReservationEntity(
                reservation.getId(),
                reservation.getCustomerName(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );
    }

    private ReservationTimesWithStatus mapToTimesWithStatus(final ResultSet resultSet, final int rowNum) throws SQLException {
        return new ReservationTimesWithStatus(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime(),
                resultSet.getBoolean("reserved")
        );
    }
}

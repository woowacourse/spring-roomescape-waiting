package roomescape.reservation.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.repository.entity.ReservationEntity;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private static final String GENERATED_ID_NOT_FOUND_MESSAGE = "생성된 id를 가져오지 못했습니다.";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.customer_name AS reservation_name,
                    r.customer_email AS reservation_email,
                    r.status AS reservation_status,
                    s.id AS slot_id,
                    s.reservation_date AS reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_slot s ON r.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                ORDER BY r.id
                """;

        return jdbcTemplate.query(sql, this::mapToDomain)
                .stream()
                .toList();
    }

    @Override
    public List<Reservation> findAllByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
            final String customerName,
            final String customerEmail,
            final LocalDateTime now
    ) {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.customer_name AS reservation_name,
                    r.customer_email AS reservation_email,
                    r.status AS reservation_status,
                    s.id AS slot_id,
                    s.reservation_date AS reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_slot s ON r.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
                WHERE r.customer_name = ?
                  AND r.customer_email = ?
                  AND r.status = 'CONFIRMED'
                  AND (s.reservation_date > ? OR (s.reservation_date = ? AND t.start_at > ?))
                ORDER BY s.reservation_date ASC
                """;

        return jdbcTemplate.query(sql,
                        this::mapToDomain,
                        customerName,
                        customerEmail,
                        Date.valueOf(now.toLocalDate()),
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
                    r.customer_name AS reservation_name,
                    r.customer_email AS reservation_email,
                    r.status AS reservation_status,
                    s.id AS slot_id,
                    s.reservation_date AS reservation_date,
                    t.id AS time_id,
                    t.start_at AS time_start_at,
                    h.id AS theme_id,
                    h.name AS theme_name,
                    h.description AS theme_description,
                    h.thumbnail_url AS theme_thumbnail_url,
                    h.price AS theme_price
                FROM reservation r
                JOIN reservation_slot s ON r.slot_id = s.id
                JOIN reservation_time t ON s.time_id = t.id
                JOIN theme h ON s.theme_id = h.id
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
                newReservation.getCustomerEmail(),
                newReservation.getSlot(),
                newReservation.getStatus()
        );
    }

    @Override
    public boolean update(final Reservation reservation) {
        final String sql = """
                UPDATE reservation
                SET slot_id = ?
                WHERE id = ?
                """;

        final int updatedCount = jdbcTemplate.update(
                sql,
                reservation.getSlotId(),
                reservation.getId()
        );

        return hasUpdatedReservation(updatedCount);
    }

    @Override
    public boolean confirm(final Long reservationId) {
        final String sql = """
                UPDATE reservation
                SET status = ?
                WHERE id = ? AND status = ?
                """;

        return jdbcTemplate.update(
                sql,
                ReservationStatus.CONFIRMED.name(),
                reservationId,
                ReservationStatus.PENDING.name()
        ) > 0;
    }

    @Override
    public boolean deletePendingById(final Long reservationId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = ? AND status = ?
                """;

        return jdbcTemplate.update(
                sql,
                reservationId,
                ReservationStatus.PENDING.name()
        ) > 0;
    }

    @Override
    public boolean deleteByIdAndSlotId(final Long reservationId, final Long slotId) {
        final String sql = """
                DELETE FROM reservation
                WHERE id = ? AND slot_id = ?
                """;

        return jdbcTemplate.update(sql, reservationId, slotId) > 0;
    }

    private static boolean hasUpdatedReservation(final int updatedCount) {
        return updatedCount > 0;
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
                LEFT JOIN reservation_slot s
                    ON s.time_id = rt.id
                   AND s.reservation_date = ?
                   AND s.theme_id = ?
                LEFT JOIN reservation r ON r.slot_id = s.id
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
                INSERT INTO reservation (customer_name, customer_email, slot_id, status)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );

            preparedStatement.setString(1, reservationEntity.name());
            preparedStatement.setString(2, reservationEntity.email());
            preparedStatement.setLong(3, reservationEntity.slotId());
            preparedStatement.setString(4, reservationEntity.status());

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(GENERATED_ID_NOT_FOUND_MESSAGE);
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
                resultSet.getString("theme_thumbnail_url"),
                resultSet.getInt("theme_price")
        );

        final ReservationSlot slot = ReservationSlot.of(
                resultSet.getLong("slot_id"),
                resultSet.getDate("reservation_date").toLocalDate(),
                reservationTime,
                theme
        );

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getString("reservation_email"),
                slot,
                ReservationStatus.valueOf(resultSet.getString("reservation_status"))
        );
    }

    private ReservationEntity toEntity(final Reservation reservation) {
        return new ReservationEntity(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getCustomerEmail(),
                reservation.getSlotId(),
                reservation.getStatus().name()
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

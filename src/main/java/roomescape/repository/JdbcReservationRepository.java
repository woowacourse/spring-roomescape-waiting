package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation save(Reservation reservationWithoutId) {
        String sql = """
                INSERT INTO reservation(name, date, time_id, theme_id, status, order_id, idempotency_key, amount,
                                        payment_key)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setString(1, reservationWithoutId.getName());
                preparedStatement.setDate(2, Date.valueOf(reservationWithoutId.getDate()));
                preparedStatement.setLong(3, reservationWithoutId.getTime().getId());
                preparedStatement.setLong(4, reservationWithoutId.getTheme().getId());
                preparedStatement.setString(5, reservationWithoutId.getStatus().name());
                preparedStatement.setString(6, reservationWithoutId.getOrderId());
                preparedStatement.setString(7, reservationWithoutId.getIdempotencyKey());
                if (reservationWithoutId.getAmount() == null) {
                    preparedStatement.setObject(8, null);
                } else {
                    preparedStatement.setLong(8, reservationWithoutId.getAmount());
                }
                preparedStatement.setString(9, reservationWithoutId.getPaymentKey());

                return preparedStatement;
            }, keyHolder);

            Long id = keyHolder.getKey().longValue();
            return Reservation.of(id, reservationWithoutId);
        } catch (DataIntegrityViolationException e) {
            throw new RoomEscapeException(DomainErrorCode.SLOT_JUST_TAKEN);
        }
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.id = (?)
                """;

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, reservationRowMapper(), id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.date = (?) AND r.time_id = (?) AND r.theme_id = (?)
                """;

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, reservationRowMapper(), date, timeId, themeId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> lockById(Long id) {
        String sql = "SELECT id FROM reservation WHERE id = ? FOR UPDATE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> lockBySlot(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT id FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                FOR UPDATE
                """;
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, Long.class, date, timeId, themeId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> lockByOrderId(String orderId) {
        String sql = "SELECT id FROM reservation WHERE order_id = ? FOR UPDATE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, orderId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findByOrderId(String orderId) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.order_id = (?)
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reservationRowMapper(), orderId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findPendingByOrderId(String orderId) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.order_id = (?) AND r.status = ?
                FOR UPDATE
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reservationRowMapper(), orderId,
                    ReservationStatus.PENDING.name()));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Reservation startPaymentConfirmation(String orderId) {
        String sql = """
                UPDATE reservation
                SET status = ?
                WHERE order_id = ? AND status IN (?, ?)
                """;

        int updated = jdbcTemplate.update(sql, ReservationStatus.PAYMENT_CONFIRMING.name(), orderId,
                ReservationStatus.PENDING.name(), ReservationStatus.PAYMENT_UNKNOWN.name());
        if (updated == 0) {
            throw new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER);
        }
        return findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    @Override
    public Reservation releasePaymentConfirmation(String orderId) {
        String sql = """
                UPDATE reservation
                SET status = ?
                WHERE order_id = ? AND status = ?
                """;

        int updated = jdbcTemplate.update(sql, ReservationStatus.PENDING.name(), orderId,
                ReservationStatus.PAYMENT_CONFIRMING.name());
        if (updated == 0) {
            throw new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER);
        }
        return findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    @Override
    public Reservation confirmPayment(String orderId, String paymentKey) {
        String sql = """
                UPDATE reservation
                SET status = ?, payment_key = ?
                WHERE order_id = ? AND status = ?
                """;

        int updated = jdbcTemplate.update(sql, ReservationStatus.CONFIRMED.name(), paymentKey,
                orderId, ReservationStatus.PAYMENT_CONFIRMING.name());
        if (updated == 0) {
            throw new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER);
        }
        return findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    @Override
    public Reservation markPaymentUnknown(String orderId) {
        String sql = """
                UPDATE reservation
                SET status = ?
                WHERE order_id = ? AND status = ?
                """;

        int updated = jdbcTemplate.update(sql, ReservationStatus.PAYMENT_UNKNOWN.name(), orderId,
                ReservationStatus.PAYMENT_CONFIRMING.name());
        if (updated == 0) {
            throw new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER);
        }
        return findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    @Override
    public void deletePendingByOrderId(String orderId) {
        String sql = "DELETE FROM reservation WHERE order_id = ? AND status = ?";

        jdbcTemplate.update(sql, orderId, ReservationStatus.PENDING.name());
    }

    @Override
    public void deleteStalePendingBefore(LocalDateTime expiresBefore) {
        String sql = "DELETE FROM reservation WHERE status = ? AND created_at < ?";

        jdbcTemplate.update(sql, ReservationStatus.PENDING.name(), expiresBefore);
    }

    @Override
    public List<Reservation> findStalePendingBefore(LocalDateTime expiresBefore) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.status = ? AND r.created_at < ?
                ORDER BY r.created_at, r.id
                FOR UPDATE
                """;

        return jdbcTemplate.query(sql, reservationRowMapper(), ReservationStatus.PENDING.name(), expiresBefore);
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.name = (?) AND r.status IN ('PENDING', 'CONFIRMED')
                """;

        return jdbcTemplate.query(sql, reservationRowMapper(), name);
    }

    @Override
    public List<Reservation> findPaymentHistoryByName(String name) {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.name = (?) AND r.order_id IS NOT NULL
                ORDER BY r.id DESC
                """;

        return jdbcTemplate.query(sql, reservationRowMapper(), name);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id, r.name, r.date, r.status, r.order_id, r.idempotency_key, r.amount, r.payment_key,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_url as theme_thumbnail_url
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.status IN ('PENDING', 'CONFIRMED')
                """;

        return jdbcTemplate.query(sql, reservationRowMapper());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = (?)";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = (?)) AS exist";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = (?)) AS exist";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    private static RowMapper<Reservation> reservationRowMapper() {
        return (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            LocalDate date = resultSet.getDate("date").toLocalDate();
            ReservationStatus status = ReservationStatus.valueOf(resultSet.getString("status"));
            String orderId = resultSet.getString("order_id");
            String idempotencyKey = resultSet.getString("idempotency_key");
            Long amount = nullableLong(resultSet.getObject("amount"));
            String paymentKey = resultSet.getString("payment_key");
            Long timeId = resultSet.getLong("time_id");
            LocalTime timeValue = resultSet.getTime("time_value").toLocalTime();
            Long themeId = resultSet.getLong("theme_id");
            String themeName = resultSet.getString("theme_name");
            String themeDescription = resultSet.getString("theme_description");
            String themeThumbnailUrl = resultSet.getString("theme_thumbnail_url");

            ReservationTime reservationTime = new ReservationTime(timeId, timeValue);
            Theme theme = new Theme(themeId, themeName, themeDescription, themeThumbnailUrl);
            return new Reservation(id, name, date, reservationTime, theme, status, orderId, idempotencyKey, amount,
                    paymentKey);
        };
    }

    private static Long nullableLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }
}

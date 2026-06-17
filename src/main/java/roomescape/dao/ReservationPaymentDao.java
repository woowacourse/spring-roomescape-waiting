package roomescape.dao;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class ReservationPaymentDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<ReservationPayment> reservationPaymentRowMapper = (rs, rowNum) -> new ReservationPayment(
            rs.getLong("payment_id"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("payment_key"),
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
                    new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"),
                            rs.getString("theme_thumbnail"))
            )
    );

    public ReservationPaymentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_payment")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationPayment save(ReservationPayment payment) {
        Reservation reservation = payment.getReservation();
        try {
            long id = jdbcInsert.executeAndReturnKey(Map.of(
                    "order_id", payment.getOrderId(),
                    "amount", payment.getAmount(),
                    "name", reservation.getName(),
                    "date", reservation.getDate(),
                    "created_at", payment.getCreatedAt(),
                    "time_id", reservation.getTime().getId(),
                    "theme_id", reservation.getTheme().getId()
            )).longValue();
            return payment.withId(id);
        } catch (DuplicateKeyException e) {
            throw new DataConflictException(e);
        }
    }

    public Optional<ReservationPayment> findByOrderId(String orderId) {
        String sql = """
                SELECT rp.id AS payment_id, rp.order_id, rp.amount, rp.payment_key,
                       rp.id AS reservation_id, rp.name, rp.date, rp.created_at,
                       t.id AS time_id, t.start_at AS time_value,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail_url AS theme_thumbnail
                FROM reservation_payment AS rp
                INNER JOIN reservation_time AS t ON rp.time_id = t.id
                INNER JOIN theme AS th ON rp.theme_id = th.id
                WHERE rp.order_id = ?
                """;
        return jdbcTemplate.query(sql, reservationPaymentRowMapper, orderId).stream().findFirst();
    }

    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_payment WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, date, timeId, themeId), 0) > 0;
    }

    public boolean existsByTimeId(long timeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_payment WHERE time_id = ?",
                Integer.class, timeId), 0) > 0;
    }

    public boolean existsByThemeId(long themeId) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_payment WHERE theme_id = ?",
                Integer.class, themeId), 0) > 0;
    }
}

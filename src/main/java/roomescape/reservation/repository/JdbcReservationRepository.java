package roomescape.reservation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private static final String BASE_SELECT = """
            SELECT r.id,
                   r.name,
                   r.time_id,
                   r.theme_id,
                   r.status,
                   r.order_id,
                   r.amount,
                   r.payment_key,
                   r.created_at,
                   rt.start_time,
                   rt.end_time,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.image_url AS theme_image_url
            FROM reservation r
            LEFT JOIN reservation_time rt ON r.time_id = rt.id
            LEFT JOIN theme t ON r.theme_id = t.id
            """;

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(BASE_SELECT, new ReservationRowMapper());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        List<Reservation> results = jdbcTemplate.query(
                BASE_SELECT + "WHERE r.id = ?",
                new ReservationRowMapper(),
                id
        );
        return results.stream().findFirst();
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(Long id) {
        jdbcTemplate.query(
                "SELECT id FROM reservation WHERE id = ? FOR UPDATE",
                (rs, rowNum) -> rs.getLong("id"),
                id
        );
        return findById(id);
    }

    @Override
    public boolean update(Long id, Long timeId, LocalDateTime now, Status status) {
        int affected = jdbcTemplate.update(
                "UPDATE reservation SET time_id = ?, created_at = ?, status = ? WHERE id = ?",
                timeId, now, status.name(), id
        );
        return affected > 0;
    }

    @Override
    public Reservation save(Reservation reservation) {
        Number id = reservationInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("order_id", reservation.getOrderId())
                .addValue("amount", reservation.getAmount())
                .addValue("created_at", reservation.getCreatedAt()));
        return reservation.withId(id.longValue());
    }

    @Override
    public Optional<Reservation> findByOrderId(String orderId) {
        List<Reservation> results = jdbcTemplate.query(
                BASE_SELECT + "WHERE r.order_id = ?",
                new ReservationRowMapper(),
                orderId
        );
        return results.stream().findFirst();
    }

    @Override
    public boolean confirmPayment(Long reservationId, String paymentKey) {
        int affected = jdbcTemplate.update(
                "UPDATE reservation SET status = ?, payment_key = ? WHERE id = ?",
                Status.CONFIRMED.name(), paymentKey, reservationId
        );
        return affected > 0;
    }

    @Override
    public boolean hasConfirmedReservation(Long themeId, ReservationTime time) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ? AND time_id = ? AND status IN (?, ?, ?))",
                Integer.class,
                themeId,
                time.getId(),
                Status.RESERVED.name(),
                Status.CONFIRMED.name(),
                Status.PAYMENT_PENDING.name()
        );
        return exists != null && exists == 1;
    }

    @Override
    public List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date) {
        return jdbcTemplate.query(
                """
                        SELECT r.time_id FROM reservation r
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.theme_id = ? AND rt.start_time >= ? AND rt.start_time < ?
                        """,
                (rs, rowNum) -> rs.getLong("time_id"),
                themeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows = jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
        return affectedRows > 0;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)",
                Integer.class,
                timeId
        );
        return exists != null && exists == 1;
    }

    @Override
    public Optional<Long> findEarliestWaiting(Long timeId, Long themeId) {
        return jdbcTemplate.query(
                "SELECT id FROM reservation "
                        + "WHERE time_id = ? "
                        + "AND theme_id = ? "
                        + "AND status = ? "
                        + "ORDER BY created_at ASC "
                        + "LIMIT 1",
                (rs, rowNum) -> rs.getLong("id"),
                timeId, themeId, Status.WAITING.name()
        ).stream().findFirst();
    }

    @Override
    public boolean promoteToReserved(Long waitingId) {
        int affected = jdbcTemplate.update(
                "UPDATE reservation SET status = ? where id = ?",
                Status.RESERVED.name(),
                waitingId);
        return affected > 0;
    }

    private static class ReservationRowMapper implements RowMapper<Reservation> {
        @Override
        public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReservationTime time = null;
            Long timeId = rs.getLong("time_id");
            if (!rs.wasNull()) {
                time = new ReservationTime(
                        timeId,
                        rs.getObject("start_time", LocalDateTime.class),
                        rs.getObject("end_time", LocalDateTime.class)
                );
            }

            Theme theme = null;
            String themeName = rs.getString("theme_name");
            if (themeName != null) {
                theme = new Theme(
                        themeName,
                        rs.getString("theme_description"),
                        rs.getString("theme_image_url")
                ).withId(rs.getLong("theme_id"));
            }

            Status status = Status.valueOf(rs.getString("status"));
            String orderId = rs.getString("order_id");
            long amountVal = rs.getLong("amount");
            Long amount = rs.wasNull() ? null : amountVal;
            LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);

            return new Reservation(
                    rs.getString("name"),
                    time,
                    theme,
                    status,
                    orderId,
                    amount,
                    createdAt
            ).withId(rs.getLong("id"));
        }
    }

    @Override
    public List<ReservationWithWaitingOrder> findAllByName(String name) {
        String sql = """
                SELECT r.id,
                       r.name,
                       r.status,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.image_url AS theme_image_url,
                       rt.id AS time_id,
                       rt.start_time,
                       rt.end_time,
                       ranked.rank AS orderWaiting
                  FROM reservation r
                  JOIN theme t ON r.theme_id = t.id
                  JOIN reservation_time rt ON r.time_id = rt.id
                  LEFT JOIN (
                      SELECT id,
                             ROW_NUMBER() OVER (
                                 PARTITION BY theme_id, time_id
                                 ORDER BY created_at ASC
                             ) AS rank
                      FROM reservation
                      WHERE status = ?
                  ) ranked ON r.id = ranked.id
                  WHERE r.name = ?;
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationTime time = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getObject("start_time", LocalDateTime.class),
                    rs.getObject("end_time", LocalDateTime.class)
            );
            Theme theme = new Theme(
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_image_url")
            ).withId(rs.getLong("theme_id"));
            return new ReservationWithWaitingOrder(
                    rs.getLong("id"),
                    rs.getString("name"),
                    ReservationTimeResponse.from(time),
                    ThemeResponse.from(theme),
                    Status.valueOf(rs.getString("status")),
                    rs.getObject("orderWaiting", Integer.class)
            );
        }, Status.WAITING.name(), name);
    }

    @Override
    public boolean isDuplicatedWithName(String name, Long themeId, ReservationTime time) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE name = ? AND theme_id = ? AND time_id = ?)",
                Integer.class,
                name,
                themeId,
                time.getId()
        );
        return exists != null && exists == 1;
    }
}

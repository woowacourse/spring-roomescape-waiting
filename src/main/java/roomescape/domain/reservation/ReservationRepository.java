package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> Reservation.of(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.of(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_start_at").toLocalTime(),
                    resultSet.getTime("time_finish_at").toLocalTime()
            ),
            Theme.of(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_image_url"),
                    resultSet.getLong("theme_price")
            ),
            ReservationStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("order_id"),
            resultSet.getLong("quoted_amount")
    );

    private final RowMapper<Long> timeMapper = (resultSet, rowNum) ->
            resultSet.getLong("time_id");

    private final RowMapper<Long> idRowMapper = (resultSet, rowNum)
            -> resultSet.getLong("theme_id");

    private final RowMapper<ReservationSummary> summaryMapper = (resultSet, rowNum) -> new ReservationSummary(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            resultSet.getTime("time_start_at").toLocalTime(),
            resultSet.getString("theme_name"),
            ReservationStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("order_id"),
            resultSet.getString("payment_key"),
            resultSet.getObject("amount", Long.class)
    );

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("order_id", reservation.getOrderId())
                .addValue("quoted_amount", reservation.getQuotedAmount());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.of(id, reservation.getName(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getStatus(), reservation.getOrderId(), reservation.getQuotedAmount());
    }

    public void updateStatus(Long id, ReservationStatus status) {
        jdbcTemplate.update("UPDATE reservation SET status = ? WHERE id = ?", status.name(), id);
    }

    public boolean existsBySlot(ReservationSlot slot) {
        String query = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class,
                slot.getDate(), slot.getTime().getId(), slot.getTheme().getId());
        return count != null && count > 0;
    }

    public List<Long> findTimeByDateAndThemeId(LocalDate date, Long themeId) {
        String query = """
                SELECT r.time_id
                FROM reservation r
                WHERE r.date = ? AND r.theme_id = ?
                """;
        return jdbcTemplate.query(query, timeMapper, date, themeId);
    }

    public int deleteById(Long id) {
        String query = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(query, id);
    }

    public boolean existsByThemeId(Long themeId) {
        String query = """
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, themeId);
        return count != null && count > 0;
    }

    public List<Long> findThemeIdsByDateRange(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT theme_id FROM reservation WHERE date BETWEEN ? AND ?";
        return jdbcTemplate.query(query, idRowMapper, startDate, endDate);
    }

    public List<ReservationSummary> findByName(String name) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date, r.status, r.order_id,
                       t.start_at AS time_start_at,
                       th.name AS theme_name,
                       p.payment_key, p.amount
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                LEFT JOIN payment p ON p.reservation_id = r.id
                WHERE r.name = ?
                """;
        return jdbcTemplate.query(query, summaryMapper, name);
    }

    public Optional<Reservation> findById(Long id) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date, r.status, r.order_id, r.quoted_amount,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    public Optional<Reservation> findByIdForUpdate(Long id) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date, r.status, r.order_id, r.quoted_amount,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                FOR UPDATE
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    public Optional<Reservation> findBySlot(ReservationSlot slot) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date, r.status, r.order_id, r.quoted_amount,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                """;
        return jdbcTemplate.query(query, rowMapper,
                        slot.getDate(), slot.getTime().getId(), slot.getTheme().getId())
                .stream().findFirst();
    }

    public Optional<Reservation> findByOrderId(String orderId) {
        String query = """
                SELECT r.id AS reservation_id, r.name, r.date, r.status, r.order_id, r.quoted_amount,
                       t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM reservation r
                JOIN reservation_time t ON r.time_id = t.id
                JOIN theme th ON r.theme_id = th.id
                WHERE r.order_id = ?
                """;
        return jdbcTemplate.query(query, rowMapper, orderId).stream().findFirst();
    }

    public void update(Reservation reservation) {
        String query = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(query, reservation.getDate(), reservation.getTime().getId(), reservation.getId());
    }
}

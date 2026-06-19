package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;

@Repository
@Transactional(readOnly = true)
public class ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getObject("date", LocalDate.class),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getObject("start_at", LocalTime.class)
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("description"),
                    resultSet.getString("url")
            ),
            ReservationStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("order_id"),
            resultSet.getString("payment_key"),
            resultSet.getLong("amount")
    );

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id, status, order_id, amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setObject(2, reservation.getDate());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setString(5, reservation.getStatus().name());
            ps.setString(6, reservation.getOrderId());
            if (reservation.getAmount() != null) {
                ps.setLong(7, reservation.getAmount());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }

            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        return findById(id).orElseThrow(() -> new IllegalStateException("예약 데이터를 조회할 수 없습니다."));
    }

    public Reservations findAll() {
        String sql = """
                    SELECT r.id,r.name,r.date,rt.id AS time_id, rt.start_at,
                        t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                        r.order_id, r.payment_key, r.amount
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                """;

        return new Reservations(jdbcTemplate.query(sql, reservationRowMapper));
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                        t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                        r.order_id, r.payment_key, r.amount
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream().findFirst();
    }

    public Optional<Reservation> findByOrderId(String orderId) {
        String sql = """
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                        t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                        r.order_id, r.payment_key, r.amount
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.order_id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, orderId)
                .stream().findFirst();
    }

    @Transactional
    public void updatePayment(Long id, String paymentKey, ReservationStatus status, String orderId, Long amount) {
        jdbcTemplate.update("UPDATE reservation SET payment_key = ?, status = ?, order_id = ?, amount = ? WHERE id = ?",
                paymentKey, status.name(), orderId, amount, id);
    }

    public List<ReservationRank> findByName(String name) {
        String sql = """
                    SELECT *
                    FROM (
                        SELECT
                            r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                            t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                            r.order_id, r.payment_key, r.amount,
                            CASE WHEN r.status = 'WAITING'
                                 THEN ROW_NUMBER() OVER (
                                     PARTITION BY r.date, r.theme_id, r.time_id, r.status 
                                     ORDER BY r.id
                                  )
                            END AS waiting_order
                        FROM reservation r
                        INNER JOIN reservation_time rt ON r.time_id = rt.id
                        INNER JOIN theme t ON r.theme_id = t.id
                    ) sub
                    WHERE sub.name = ?
                """;

        return jdbcTemplate.query(sql,
                (resultSet, rowNum) -> {
                    Reservation reservation = reservationRowMapper.mapRow(resultSet, rowNum);
                    return new ReservationRank(reservation, resultSet.getLong("waiting_order"));
                },
                name
        );
    }

    public Reservations findByDateAndThemeId(LocalDate date, long themeId) {
        String sql = """
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                        t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                        r.order_id, r.payment_key, r.amount
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.date = ? AND r.theme_id = ?
                """;

        return new Reservations(jdbcTemplate.query(sql, reservationRowMapper, date, themeId));
    }

    @Transactional
    public Reservations findByDateAndThemeAndTimeForUpdate(LocalDate date, long themeId, long timeId) {
        String sql = """
                    SELECT r.id, r.name, r.date, rt.id AS time_id, rt.start_at,
                        t.id AS theme_id, t.name AS theme_name, t.description, t.url, r.status,
                        r.order_id, r.payment_key, r.amount
                    FROM reservation r
                    INNER JOIN reservation_time rt ON r.time_id = rt.id
                    INNER JOIN theme t ON r.theme_id = t.id
                    WHERE r.date = ? AND r.theme_id = ? AND r.time_id = ?
                    FOR UPDATE
                """;

        return new Reservations(jdbcTemplate.query(sql, reservationRowMapper, date, themeId, timeId));
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}

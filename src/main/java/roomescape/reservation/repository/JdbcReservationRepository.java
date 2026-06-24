package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.common.domain.ReservationSlot;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationIdResponse;
import roomescape.reservation.dto.ReservationWithPaymentResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> Reservation.restore(
            resultSet.getLong("reservation_id"),
            resultSet.getString("name"),
            new ReservationSlot(
                    resultSet.getDate("date").toLocalDate(),
                    ReservationTime.restore(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_start_at").toLocalTime(),
                            resultSet.getTime("time_finish_at").toLocalTime()
                    ),
                    Theme.restore(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_image_url"),
                            resultSet.getInt("theme_price")
                    )
            ),
            PaymentStatus.valueOf(resultSet.getString("status"))
    );

    private final RowMapper<Long> idMapper = (resultSet, rowNum) -> (
            resultSet.getLong("id")
    );

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.restore(id, reservation.getName(),
                new ReservationSlot(reservation.getDate(), reservation.getTime(),
                        reservation.getTheme()), reservation.getStatus());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String query = """
                SELECT r.id as reservation_id, r.name, r.date,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url, t.price as theme_price,
                       r.status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String query = """
                SELECT r.id as reservation_id, r.name, r.date,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url, t.price as theme_price,
                       r.status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(query, rowMapper);
    }

    @Override
    public List<Reservation> findConfirmedByName(String name) {
        // 내 예약 조회는 결제 완료(CONFIRMED)된 예약만 보여준다. 결제 대기(PENDING)는 아직 확정이 아니므로 제외.
        String query = """
                SELECT r.id as reservation_id, r.name, r.date,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.id as theme_id, t.name as theme_name, t.description as theme_description, t.image_url as theme_image_url, t.price as theme_price,
                       r.status
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.name = ? AND r.status = ?
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(query, rowMapper, name, PaymentStatus.CONFIRMED.name());
    }

    @Override
    public void update(Long id, ReservationSlot slot) {
        String query = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(query, slot.date(), slot.time().getId(), id);
    }

    @Override
    public boolean isBooked(ReservationSlot slot) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, slot.date(), slot.time().getId(),
                slot.theme().getId());
        return count != null && count > 0;
    }

    @Override
    public boolean isReservedBy(ReservationSlot slot, String name) {
        String query = "select count(*) from reservation where name = ? and date = ? and time_id = ? and theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, name, slot.date(), slot.time().getId(),
                slot.theme().getId());
        return count != null && count > 0;
    }

    @Override
    public boolean isBookedByOther(ReservationSlot slot, Long id) {
        String query = "select count(*) from reservation where date = ? and time_id = ? and theme_id = ? and id != ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, slot.date(), slot.time().getId(),
                slot.theme().getId(), id);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String query = "delete from reservation where id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public ReservationIdResponse findIdBySlot(LocalDate date, Long themeId, Long timeId) {
        String query = "select id from reservation where date = ? and theme_id = ? and time_id = ?";
        return ReservationIdResponse.from(jdbcTemplate.query(query, idMapper, date, themeId, timeId).getFirst());
    }

    @Override
    public void updateStatus(Long reservationId, PaymentStatus status) {
        String query = "UPDATE reservation SET status = ? WHERE id = ?";
        jdbcTemplate.update(query, status.name(), reservationId);
    }

    @Override
    public List<ReservationWithPaymentResponse> findWithPaymentByName(String name) {
        String query = """
                SELECT r.id as reservation_id, r.name, r.date,
                       rt.id as time_id, rt.start_at as time_start_at, rt.finish_at as time_finish_at,
                       t.name as theme_name,
                       r.status,
                       p.order_id, p.payment_key, p.amount as payment_amount
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                LEFT JOIN payment p ON p.reservation_id = r.id
                WHERE r.name = ? AND r.status IN ('CONFIRMED', 'PAYMENT_UNCERTAIN', 'PAYMENT_PENDING')
                ORDER BY r.date DESC, rt.start_at DESC
                """;
        return jdbcTemplate.query(query, (rs, rowNum) -> new ReservationWithPaymentResponse(
                rs.getLong("reservation_id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                new TimeResponse(
                        rs.getLong("time_id"),
                        rs.getTime("time_start_at").toLocalTime()
                ),
                rs.getString("theme_name"),
                PaymentStatus.valueOf(rs.getString("status")),
                rs.getString("order_id"),
                rs.getString("payment_key"),
                rs.getLong("payment_amount")
        ), name);
    }
}

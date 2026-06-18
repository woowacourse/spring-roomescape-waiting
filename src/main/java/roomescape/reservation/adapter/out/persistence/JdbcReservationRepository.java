package roomescape.reservation.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private static final List<String> ACTIVE_PAYMENT_STATUSES = List.of(
            ReservationStatus.PENDING.name(),
            ReservationStatus.CONFIRMED.name(),
            ReservationStatus.PAYMENT_CHECK_REQUIRED.name()
    );

    private final NamedParameterJdbcTemplate template;

    private final RowMapper<ReservationDetailProjection> reservationDetailFindRowMapper = (resultSet, rowNum) ->
            new ReservationDetailProjection(
                    resultSet.getLong("reservation_id"),
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail_url"),
                    resultSet.getInt("theme_price"),
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    ReservationStatus.valueOf(resultSet.getString("status")),
                    resultSet.getString("order_id"),
                    resultSet.getInt("amount"),
                    resultSet.getString("payment_key")
            );

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url"),
                resultSet.getInt("theme_price")
        );
        Slot slot = Slot.of(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                time,
                theme,
                resultSet.getInt("slot_price")
        );
        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getLong("member_id"),
                slot,
                ReservationStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("order_id"),
                resultSet.getString("idempotency_key"),
                resultSet.getInt("amount"),
                resultSet.getString("payment_key")
        );
    };

    @Override
    public Reservation save(Reservation reservation) {
        String insertReservationSql = """
                INSERT INTO reservation(member_id, slot_id, status, order_id, idempotency_key, amount, payment_key)
                VALUES (:memberId, :slotId, :status, :orderId, :idempotencyKey, :amount, :paymentKey)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", reservation.getMemberId())
                .addValue("slotId", reservation.getSlotId())
                .addValue("status", reservation.getStatus().name())
                .addValue("orderId", reservation.getOrderId())
                .addValue("idempotencyKey", reservation.getIdempotencyKey())
                .addValue("amount", reservation.getAmount())
                .addValue("paymentKey", reservation.getPaymentKey());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(insertReservationSql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("reservation 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return Reservation.of(
                keyHolder.getKey().longValue(),
                reservation.getMemberId(),
                reservation.getSlot(),
                reservation.getStatus(),
                reservation.getOrderId(),
                reservation.getIdempotencyKey(),
                reservation.getAmount(),
                reservation.getPaymentKey()
        );
    }

    @Override
    public boolean confirmPayment(long reservationId, String orderId, String paymentKey) {
        String sql = """
                UPDATE reservation
                SET status = 'CONFIRMED', payment_key = :paymentKey
                WHERE id = :reservationId
                AND order_id = :orderId
                AND status IN ('PENDING', 'PAYMENT_CHECK_REQUIRED')
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("orderId", orderId)
                .addValue("paymentKey", paymentKey);

        return template.update(sql, params) == 1;
    }

    @Override
    public boolean markPaymentCheckRequired(long reservationId, String orderId, String paymentKey) {
        String sql = """
                UPDATE reservation
                SET status = 'PAYMENT_CHECK_REQUIRED', payment_key = :paymentKey
                WHERE id = :reservationId
                AND order_id = :orderId
                AND status IN ('PENDING', 'PAYMENT_CHECK_REQUIRED')
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("orderId", orderId)
                .addValue("paymentKey", paymentKey);

        return template.update(sql, params) == 1;
    }

    @Override
    public void markPaymentFailed(long reservationId, String orderId, String paymentKey) {
        String sql = """
                UPDATE reservation
                SET status = 'PAYMENT_FAILED', payment_key = :paymentKey
                WHERE id = :reservationId
                AND order_id = :orderId
                AND status = 'PENDING'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("orderId", orderId)
                .addValue("paymentKey", paymentKey);

        template.update(sql, params);
    }

    @Override
    public void markPendingPaymentFailedByOrderIdAndMemberId(String orderId, long memberId) {
        String sql = """
                UPDATE reservation
                SET status = 'PAYMENT_FAILED'
                WHERE order_id = :orderId
                AND member_id = :memberId
                AND status = 'PENDING'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("memberId", memberId);

        template.update(sql, params);
    }

    @Override
    public List<ReservationDetailProjection> findAll() {
        String sql = """
                 SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price,
                    rt.id AS time_id,
                    rt.start_at,
                    r.status,
                    r.order_id,
                    r.amount,
                    r.payment_key
                FROM reservation r
                JOIN slot s ON r.slot_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON r.member_id = m.id
                ORDER BY r.id
                """;

        return template.query(sql, reservationDetailFindRowMapper);
    }

    @Override
    public Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        String sql = """
                SELECT
                    s.time_id
                FROM slot s
                LEFT JOIN reservation r
                    ON s.id = r.slot_id
                    AND r.status IN (:activeStatuses)
                WHERE s.date = :date
                AND s.theme_id = :themeId
                AND r.id IS NOT NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId)
                .addValue("activeStatuses", ACTIVE_PAYMENT_STATUSES);

        return Set.copyOf(template.query(sql, params,
                (rs, rowNum) -> rs.getLong("time_id")));
    }

    @Override
    public List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price,
                    rt.id AS time_id,
                    rt.start_at,
                    r.status,
                    r.order_id,
                    r.amount,
                    r.payment_key
                FROM reservation r
                JOIN slot s ON r.slot_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON r.member_id = m.id
                WHERE m.id = :memberId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return template.query(sql, params, reservationDetailFindRowMapper);
    }

    @Override
    public void deleteById(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = :reservationId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId);
        template.update(sql, params);
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.member_id,
                    r.status,
                    r.order_id,
                    r.idempotency_key,
                    r.amount,
                    r.payment_key,
                    s.id AS slot_id,
                    s.price AS slot_price,
                    s.date,
                    rt.id AS time_id,
                    rt.start_at,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price
                FROM reservation r
                JOIN slot s ON r.slot_id = s.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                WHERE r.id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservationId);

        return template.query(sql, params, reservationRowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByOrderId(String orderId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.member_id,
                    r.status,
                    r.order_id,
                    r.idempotency_key,
                    r.amount,
                    r.payment_key,
                    s.id AS slot_id,
                    s.price AS slot_price,
                    s.date,
                    rt.id AS time_id,
                    rt.start_at,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    t.price AS theme_price
                FROM reservation r
                JOIN slot s ON r.slot_id = s.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN theme t ON s.theme_id = t.id
                WHERE r.order_id = :orderId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);

        return template.query(sql, params, reservationRowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsBySlotId(long slotId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE slot_id = :slotId
                    AND status IN (:activeStatuses)
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", slotId)
                .addValue("activeStatuses", ACTIVE_PAYMENT_STATUSES);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public boolean existsByMemberIdAndSlotId(long memberId, long slotId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE member_id = :memberId
                    AND slot_id = :slotId
                    AND status IN (:activeStatuses)
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("slotId", slotId)
                .addValue("activeStatuses", ACTIVE_PAYMENT_STATUSES);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }

}

package roomescape.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.reservationStatus.*;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("r_id"),
            rs.getString("name"),
            rs.getLong("theme_slot_id"),
            rs.getObject("date", LocalDate.class),
            new Time(rs.getLong("t_id"), rs.getObject("start_at", LocalTime.class)),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url"),
                    rs.getLong("theme_price")
            ),
            toStatus(rs.getString("status")),
            rs.getString("order_id"),
            rs.getObject("amount", Long.class)
    );

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id AS theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id as theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, reservationId).stream().findFirst();
    }

    @Override
    public Optional<Reservation> findByOrderId(String orderId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id as theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                WHERE r.order_id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, orderId).stream().findFirst();
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id as theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                WHERE r.name = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, name).stream().toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> params = createParams(reservation);
        Long reservationId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Reservation.of(reservationId, reservation);
    }

    private Map<String, Object> createParams(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", reservation.getName());
        params.put("theme_slot_id", reservation.getThemeSlotId());
        params.put("status", reservation.getReservationStatusName());
        params.put("order_id", reservation.getOrderId());
        params.put("amount", reservation.getAmount());
        return params;
    }

    @Override
    public void deleteById(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservationId);
    }

    @Override
    public boolean existsByThemeSlotId(long themeSlotId) {
        String sql = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM reservation
                            WHERE theme_slot_id = ?
                            AND status != 'CANCELLED'
                        )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeSlotId));
    }

    @Override
    public void updateStatus(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET status = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                reservation.getReservationStatusName(),
                reservation.getId()
        );
    }

    @Override
    public void updateThemeSlot(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET theme_slot_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                reservation.getThemeSlotId(),
                reservation.getId()
        );
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM reservation r
                                INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
                            WHERE ts.theme_id = ?
                        )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM reservation r
                                INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
                            WHERE ts.time_id = ?
                        )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    private ReservationStatus toStatus(String statue) {
        return switch (statue) {
            case "PENDING" -> PendingStatus.getInstance();
            case "CONFIRMED" -> ConfirmedStatus.getInstance();
            case "COMPLETED" -> CompletedStatus.getInstance();
            case "CANCELLED" -> CancelledStatus.getInstance();
            default -> throw new IllegalArgumentException("존재하지 않는 예약 상태입니다.");
        };
    }

    @Override
    public List<Reservation> findByThemeSlotAndPending(Long themeSlotId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id as theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                WHERE ts.id = ?
                AND r.status = 'PENDING'
                ORDER BY r.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, themeSlotId).stream().toList();
    }

    @Override
    public Optional<Reservation> findRecentReservationByThemeSlot(Long themeSlotId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.status,
                    r.order_id,
                    r.amount,
                    ts.id AS theme_slot_id,
                    ts.date,
                    ts.is_reserved,
                    t.id AS t_id,
                    t.start_at,
                    theme.id as theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url,
                    theme.price AS theme_price
                FROM
                    reservation r
                        INNER JOIN
                        theme_slot ts ON r.theme_slot_id = ts.id
                        INNER JOIN
                        time t ON ts.time_id = t.id
                        INNER JOIN
                        theme theme ON ts.theme_id = theme.id
                WHERE ts.id = ?
                AND r.status = 'PENDING'
                ORDER BY r.id
                LIMIT 1
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, themeSlotId).stream().findFirst();
    }
}

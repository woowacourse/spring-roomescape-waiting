package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingOrder;
import roomescape.domain.ReservationWithWaitingOrder;

@Repository
public class JdbcReservationRepository implements ReservationRepository, LockedReservationWriter {

    private static final String CANCELED = ReservationStatus.CANCELED.name();
    private static final String WAITING = ReservationStatus.WAITING.name();
    private static final String CONFIRMED = ReservationStatus.CONFIRMED.name();

    private static final String SELECT_BASE = """
            SELECT
                r.id AS reservation_id,
                r.reserver_name AS reserver_name,
                r.date AS reservation_date,
                r.status AS status,
                t.id AS time_id,
                t.start_at AS time_start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail
            FROM reservation r
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            """;

    private static final String SELECT_BASE_WITH_WAITING_ORDER = """
            SELECT
                r.id           AS reservation_id,
                r.reserver_name AS reserver_name,
                r.date         AS reservation_date,
                r.status       AS status,
                t.id           AS time_id,
                t.start_at     AS time_start_at,
                th.id          AS theme_id,
                th.name        AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail,
                CASE WHEN r.status = 'WAITING' THEN
                    (SELECT COUNT(*)
                       FROM reservation r2
                      WHERE r2.date = r.date
                        AND r2.time_id = r.time_id
                        AND r2.theme_id = r.theme_id
                        AND r2.status <> 'CANCELED'
                        AND (r2.enqueued_at < r.enqueued_at
                             OR (r2.enqueued_at = r.enqueued_at AND r2.id < r.id)))
                ELSE 0
                END AS waiting_order
            FROM reservation r
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            """;

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("reserver_name"),
            rs.getDate("reservation_date").toLocalDate(),
            new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime()
            ),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            ),
            ReservationStatus.valueOf(rs.getString("status"))
    );

    private static final RowMapper<ReservationWithWaitingOrder> RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER = (rs, rowNum) -> new ReservationWithWaitingOrder(
            rs.getLong("reservation_id"),
            rs.getString("reserver_name"),
            rs.getDate("reservation_date").toLocalDate(),
            new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime()
            ),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            ),
            ReservationStatus.valueOf(rs.getString("status")),
            new WaitingOrder(rs.getLong("waiting_order"))
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationWithWaitingOrder> findAllActive() {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.status <> '" + CANCELED + "'";
        return jdbcTemplate.query(sql, RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER);
    }

    @Override
    public List<ReservationWithWaitingOrder> findByReserverName(String reserverName) {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.reserver_name = ?";
        return jdbcTemplate.query(sql, RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER, reserverName);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = SELECT_BASE + " WHERE r.id = ?";
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, RESERVATION_ROW_MAPPER, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationWithWaitingOrder save(Reservation reservation) {
        String sql = "INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at) "
                + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getReserverName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            ps.setString(5, reservation.getStatus().name());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findWithWaitingOrderById(id).orElseThrow();
    }

    @Override
    public ReservationWithWaitingOrder updateAndRequeue(Reservation reservation) {
        String sql = "UPDATE reservation "
                + "SET reserver_name = ?, date = ?, time_id = ?, theme_id = ?, status = ?, "
                + "enqueued_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";
        jdbcTemplate.update(
                sql,
                reservation.getReserverName(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStatus().name(),
                reservation.getId()
        );
        return findWithWaitingOrderById(reservation.getId()).orElseThrow();
    }

    @Override
    public Optional<ReservationWithWaitingOrder> findWithWaitingOrderById(Long id) {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.id = ?";
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> T executeWithThemeLock(Long themeId, ThemeLockedAction<T> action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                    "테마 락은 트랜잭션 안에서만 획득할 수 있습니다: themeId=" + themeId);
        }
        Optional<Theme> lockedTheme = lockTheme(themeId);
        return action.execute(lockedTheme, this);
    }

    private Optional<Theme> lockTheme(Long themeId) {
        String sql = "SELECT id, name, description, thumbnail_url FROM theme WHERE id = ? FOR UPDATE";
        try {
            Theme theme = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail_url")
            ), themeId);
            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void cancel(Long id) {
        jdbcTemplate.update(
                "UPDATE reservation SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                CANCELED, id
        );
    }

    @Override
    public boolean promoteEarliestWaiting(LocalDate date, Long timeId, Long themeId) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM reservation "
                        + "WHERE date = ? AND time_id = ? AND theme_id = ? AND status = ? "
                        + "ORDER BY enqueued_at, id LIMIT 1",
                (rs, rowNum) -> rs.getLong("id"),
                Date.valueOf(date), timeId, themeId, WAITING
        );
        if (ids.isEmpty()) {
            return false;
        }
        jdbcTemplate.update(
                "UPDATE reservation SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                CONFIRMED, ids.getFirst()
        );
        return true;
    }

    @Override
    public boolean existsActiveConfirmed(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation "
                        + "WHERE date = ? AND time_id = ? AND theme_id = ? AND status = ?",
                Integer.class,
                Date.valueOf(date), timeId, themeId, CONFIRMED
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByReserverNameAndDateAndTimeIdAndThemeId(String reserverName, LocalDate date, Long timeId,
                                                                  Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation "
                        + "WHERE reserver_name = ? AND date = ? AND time_id = ? AND theme_id = ? AND status <> ?",
                Integer.class,
                reserverName, Date.valueOf(date), timeId, themeId, CANCELED
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
            String reserverName, LocalDate date, Long timeId, Long themeId, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation "
                        + "WHERE reserver_name = ? AND date = ? AND time_id = ? AND theme_id = ? "
                        + "AND status <> ? AND id <> ?",
                Integer.class,
                reserverName, Date.valueOf(date), timeId, themeId, CANCELED, id
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE time_id = ?",
                Integer.class,
                timeId
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE theme_id = ?",
                Integer.class,
                themeId
        );
        return count != null && count > 0;
    }
}

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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.ReservationWithWaitingOrder;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String SELECT_BASE = """
            SELECT
                r.id AS reservation_id,
                r.reserver_name AS reserver_name,
                r.date AS reservation_date,
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
                t.id           AS time_id,
                t.start_at     AS time_start_at,
                th.id          AS theme_id,
                th.name        AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail,
                (SELECT COUNT(*)
                   FROM reservation r2
                  WHERE r2.date = r.date
                    AND r2.time_id = r.time_id
                    AND r2.theme_id = r.theme_id
                    AND r2.created_at < r.created_at) AS waiting_order
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
            )
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
            rs.getLong("waiting_order")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationWithWaitingOrder> findAll() {
        return jdbcTemplate.query(SELECT_BASE_WITH_WAITING_ORDER, RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER);
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
        String sql = "INSERT INTO reservation (reserver_name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getReserverName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findWithWaitingOrderById(id);
    }

    @Override
    public ReservationWithWaitingOrder update(Reservation reservation) {
        String sql = "UPDATE reservation SET reserver_name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                reservation.getReserverName(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );
        return findWithWaitingOrderById(reservation.getId());
    }

    private ReservationWithWaitingOrder findWithWaitingOrderById(Long id) {
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " WHERE r.id = ?";
        return jdbcTemplate.queryForObject(sql, RESERVATION_WITH_WAITING_ORDER_ROW_MAPPER, id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
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
    public boolean existsByReserverNameAndDateAndTimeIdAndThemeId(String reserverName, LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE reserver_name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                reserverName, Date.valueOf(date), timeId, themeId
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND id <> ?",
                Integer.class,
                Date.valueOf(date), timeId, themeId, id
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

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
import roomescape.service.exception.ResourceNotFoundException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String SELECT_BASE = """
            SELECT
                r.id AS reservation_id,
                r.name AS reservation_name,
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
                r.name         AS reservation_name,
                r.date         AS reservation_date,
                t.id           AS time_id,
                t.start_at     AS time_start_at,
                th.id          AS theme_id,
                th.name        AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail,
                0              AS waiting_order
            FROM reservation r
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            
            UNION ALL
            
            SELECT
                w.id           AS reservation_id,
                w.name         AS reservation_name,
                w.date         AS reservation_date,
                t.id           AS time_id,
                t.start_at     AS time_start_at,
                th.id          AS theme_id,
                th.name        AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail,
                (SELECT COUNT(*) + 1 
                   FROM waiting w2 
                  WHERE w2.date = w.date 
                    AND w2.time_id = w.time_id 
                    AND w2.theme_id = w.theme_id 
                    AND w2.id < w.id) AS waiting_order
            FROM waiting w
            INNER JOIN reservation_time t ON w.time_id = t.id
            INNER JOIN theme th ON w.theme_id = th.id
            """;

    private final RowMapper<Reservation> reservationRowMapper =
            (rs, rowNum) -> new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
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

    private final RowMapper<ReservationWithWaitingOrder> reservationWithWaitingOrderRowMapper =
            (rs, rowNum) -> new ReservationWithWaitingOrder(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
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
        String sql = SELECT_BASE_WITH_WAITING_ORDER + " ORDER BY reservation_date, time_start_at, waiting_order";
        return jdbcTemplate.query(sql, reservationWithWaitingOrderRowMapper);
    }

    @Override
    public List<ReservationWithWaitingOrder> findByName(String name) {
        String sql = """
            SELECT * FROM (
            """ + SELECT_BASE_WITH_WAITING_ORDER + """
            ) all_res
            WHERE all_res.reservation_name = ?
            ORDER BY all_res.reservation_date, all_res.time_start_at, all_res.waiting_order
            """;
        return jdbcTemplate.query(sql, reservationWithWaitingOrderRowMapper, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = SELECT_BASE + " WHERE r.id = ?";
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationWithWaitingOrder save(Reservation reservation) {
        String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id)
                SELECT ?, ?, t.id, th.id
                FROM reservation_time t, theme th
                WHERE t.id = ? AND th.id = ?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        if (affectedRows == 0) {
            throw new ResourceNotFoundException("시간 또는 테마가 존재하지 않아 예약을 생성할 수 없습니다.");
        }

        Long id = keyHolder.getKey().longValue();
        return new ReservationWithWaitingOrder(
                id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                0L
        );
    }

    @Override
    public ReservationWithWaitingOrder update(Reservation reservation) {
        String sql = "UPDATE reservation SET name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                reservation.getName(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );
        return new ReservationWithWaitingOrder(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                0L
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = ?)",
                Boolean.class,
                id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, Long timeId, Long themeId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?)",
                Boolean.class,
                name, Date.valueOf(date), timeId, themeId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ? AND id <> ?)",
                Boolean.class,
                Date.valueOf(date), timeId, themeId, id
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = ?)",
                Boolean.class,
                timeId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?)",
                Boolean.class,
                themeId
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean hasReservationOnSlot(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS
                (
                    SELECT 1 FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?
                    UNION ALL
                    SELECT 1 FROM waiting WHERE date = ? AND time_id = ? AND theme_id = ?
                )
                """;
        Boolean hasReservation = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                date, timeId, themeId,
                date, timeId, themeId
        );
        return Boolean.TRUE.equals(hasReservation);
    }
}

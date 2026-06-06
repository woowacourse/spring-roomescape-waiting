package roomescape.infra;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.exception.ReservationSlotAlreadyOccupiedException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );
        return new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
    };

    private final RowMapper<ReservationWithStatus> reservationWithStatusRowMapper = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );

        return new ReservationWithStatus(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getObject("waiting_order", Integer.class)
        );
    };

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id as reservation_id, r.name, r.date,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id
                ORDER BY r.date DESC, time_value ASC;
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<ReservationWithStatus> findByName(String name) {
        String sql = """
                SELECT *
                FROM (
                    SELECT r.id as id, r.name as name, r.date as date,
                           t.id as time_id, t.start_at as time_value,
                           th.id as theme_id, th.name as theme_name,
                           th.description as theme_description, th.thumbnail_image_url as theme_thumbnail,
                           'RESERVED' AS status,
                           NULL AS waiting_order
                    FROM reservation as r
                    INNER JOIN reservation_time as t ON r.time_id = t.id
                    INNER JOIN theme as th ON r.theme_id = th.id
                    WHERE r.name = ?
                
                    UNION ALL
                
                    SELECT w.id as id, w.name as name, w.date as date,
                           t.id as time_id, t.start_at as time_value,
                           th.id as theme_id, th.name as theme_name,
                           th.description as theme_description, th.thumbnail_image_url as theme_thumbnail,
                           'WAITING' as status,
                           (
                               SELECT COUNT(*) + 1
                                FROM waitlist before_w
                                WHERE before_w.date = w.date
                                    AND before_w.time_id = w.time_id
                                    AND before_w.theme_id = w.theme_id
                                    AND (
                                        before_w.created_at < w.created_at
                                        OR (
                                            before_w.created_at = w.created_at
                                            AND before_w.id < w.id
                                        )
                                    )
                           ) as waiting_order
                    FROM waitlist as w
                    INNER JOIN reservation_time as t ON w.time_id = t.id
                    INNER JOIN theme as th ON w.theme_id = th.id
                    WHERE w.name = ?
                ) my_reservations
                ORDER BY date DESC, time_value ASC;
                """;

        return jdbcTemplate.query(sql, reservationWithStatusRowMapper, name, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT r.id as reservation_id, r.name, r.date,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id
                WHERE r.id = ?;
                """;

        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Set<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT time_id
                FROM reservation
                WHERE date = ? AND theme_id = ?;
                """;
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, date, themeId));
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBy(Reservation reservation) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?;
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySameUser(Reservation reservation) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?;
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );
        return count != null && count > 0;
    }

    @Override
    public Long save(Reservation reservation) {
        try {
            return insert(reservation);
        } catch (DuplicateKeyException e) {
            if (isReservationSlotUniqueViolation(e)) {
                throw new ReservationSlotAlreadyOccupiedException(e);
            }
            throw e;
        }
    }

    public Long insert(Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private boolean isReservationSlotUniqueViolation(DuplicateKeyException e) {
        String message = e.getMostSpecificCause().getMessage();

        return message != null && message.contains("UK_RESERVATION_SLOT");
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public void updateDateTime(Reservation updated) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql, updated.getDate(), updated.getTime().getId(), updated.getId());
    }
}

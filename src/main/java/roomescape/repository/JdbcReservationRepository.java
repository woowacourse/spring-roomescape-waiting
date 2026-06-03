package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT 
                    r.id AS r_id, 
                    r.name, 
                    r.date, 
                    r.created_at,
                    t.id AS t_id, 
                    t.start_at,
                    theme.id AS theme_id,
                    theme.name AS theme_name,
                    theme.description AS theme_description,
                    theme.thumbnail_url AS theme_thumbnail_url
                FROM reservation r 
                INNER JOIN time_slot t ON r.time_id = t.id 
                INNER JOIN theme theme  ON r.theme_id = theme.id
                """;
        return jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.date,
                    r.created_at,
                    ts.id AS t_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN time_slot ts ON r.time_id = ts.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.id = ?
                """;
        List<Reservation> reservations = jdbcTemplate.query(sql, rowMapper(), reservationId);
        return Optional.ofNullable(DataAccessUtils.singleResult(reservations));
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.date,
                    r.created_at,
                    t.id AS t_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN time_slot t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.name = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), name);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(reservation);
        long reservationId = insert.executeAndReturnKey(params).longValue();
        return new Reservation(
                reservationId,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTimeSlot(),
                reservation.getTheme(),
                reservation.getCreatedAt()
        );
    }

    @Override
    public void deleteById(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservationId);
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT
                    r.id AS r_id,
                    r.name,
                    r.date,
                    r.created_at,
                    ts.id AS t_id,
                    ts.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN time_slot ts ON r.time_id = ts.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.date = ? 
                  AND r.time_id = ? 
                  AND r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper(), date, timeId, themeId)
                .stream()
                .findAny();
    }

    @Override
    public int update(Reservation reservation) {
        String sql = "UPDATE reservation SET name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        return jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTimeSlot().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT count(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT count(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT count(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != null && count > 0;
    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private Map<String, Object> createParams(Reservation reservation) {
        return Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "time_id", reservation.getTimeSlot().getId(),
                "theme_id", reservation.getTheme().getId(),
                "created_at", reservation.getCreatedAt()
        );
    }

    private RowMapper<Reservation> rowMapper() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("r_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                new TimeSlot(
                        rs.getLong("t_id"),
                        rs.getObject("start_at", LocalTime.class)),
                new Theme(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail_url")
                ),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
}

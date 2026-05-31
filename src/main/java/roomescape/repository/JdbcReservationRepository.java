package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.ReservationNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT 
                    r.id AS r_id, 
                    r.name, 
                    r.date, 
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
                    t.id AS t_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN time_slot t ON r.time_id = t.id
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
        Map<String, Object> params = createParams(reservation);
        long reservationId = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(
                reservationId,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTimeSlot(),
                reservation.getTheme()
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
                    t.id AS t_id,
                    t.start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail_url AS theme_thumbnail_url
                FROM reservation r
                INNER JOIN time_slot t ON r.time_id = t.id
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
    public Reservation update(Reservation reservation) {
        String sql = "UPDATE reservation SET name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        int columns = jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTimeSlot().getId(),
                reservation.getTheme().getId(),
                reservation.getId()
        );
        if (columns == 0) {
            throw new ReservationNotFoundException(reservation.getId());
        }
        return reservation;
    }

    private Map<String, Object> createParams(Reservation reservation) {
        return Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "time_id", reservation.getTimeSlot().getId(),
                "theme_id", reservation.getTheme().getId()
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
                )
        );
    }
}

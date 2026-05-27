package roomescape.infra;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

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
    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT r.id as reservation_id, r.name, r.date,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id
                WHERE r.name = ?
                ORDER BY r.date DESC, time_value ASC;
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, name);
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
    public Long save(Reservation reservation) {
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

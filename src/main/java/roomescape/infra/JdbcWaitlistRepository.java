package roomescape.infra;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.repository.WaitlistRepository;

@Repository
public class JdbcWaitlistRepository implements WaitlistRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Waitlist> wailtListRowMapper = (rs, rowNum) -> {
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
        return new Waitlist(
                rs.getLong("waitlist_id"),
                rs.getString("name"),
                rs.getDate("date").toLocalDate(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                time,
                theme
        );
    };

    @Override
    public Optional<Waitlist> findById(Long id) {
        String sql = """
                SELECT w.id as waitlist_id, w.name, w.date, w.created_at,
                       t.id as time_id, t.start_at as time_value,
                       th.id as theme_id, th.name as theme_name,
                       th.description as theme_description, th.thumbnail_image_url as theme_thumbnail
                FROM waitlist as w
                INNER JOIN reservation_time as t ON w.time_id = t.id
                INNER JOIN theme as th ON w.theme_id = th.id
                WHERE w.id = ?;
                """;

        List<Waitlist> results = jdbcTemplate.query(sql, wailtListRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public int countBefore(Waitlist waitlist) {
        String sql = """
                SELECT COUNT(*)
                FROM waitlist
                WHERE date = ?
                    AND time_id = ?
                    AND theme_id = ?
                    AND created_at < ?;
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                waitlist.getDate(),
                waitlist.getTime().getId(),
                waitlist.getTheme().getId(),
                waitlist.getCreatedAt()
        );

        return count == null ? 0 : count;
    }

    @Override
    public boolean existsBySameUser(Reservation reservation) {
        String sql = """
                SELECT COUNT(*)
                FROM waitlist
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
        String sql = "INSERT INTO waitlist (name, date, created_at, time_id, theme_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, reservation.getTime().getId());
            ps.setLong(5, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM waitlist WHERE id = ?", id);
    }
}

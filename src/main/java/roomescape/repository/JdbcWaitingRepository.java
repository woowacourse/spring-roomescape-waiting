package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Waiting> rowMapper = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.withId(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );
        Theme theme = Theme.withId(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail_url")
        );
        return Waiting.withId(
                rs.getLong("id"),
                rs.getString("name"),
                LocalDate.parse(rs.getString("date")),
                time,
                theme,
                rs.getInt("wait_order")
        );
    };

    @Override
    public Waiting save(Waiting w) {
        String sql = """
                INSERT INTO waiting (name, date, time_id, theme_id, wait_order)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, w.getName());
            ps.setString(2, w.getDate().toString());
            ps.setLong(3, w.getTime().getId());
            ps.setLong(4, w.getTheme().getId());
            ps.setInt(5, w.getOrderIndex());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return Waiting.withId(id, w.getName(), w.getDate(),
                w.getTime(), w.getTheme(), w.getOrderIndex());
    }

    @Override
    public List<Waiting> findBySlot(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT w.id, w.name, w.date, w.wait_order,
                       t.id AS time_id, t.start_at,
                       th.id AS theme_id, th.name AS theme_name, th.description, th.thumbnail_url
                  FROM waiting w
                  JOIN reservation_time t ON w.time_id = t.id
                  JOIN theme th ON w.theme_id = th.id
                 WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                 ORDER BY w.wait_order ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, date.toString(), timeId, themeId);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        String sql = """
                SELECT w.id, w.name, w.date, w.wait_order,
                       t.id AS time_id, t.start_at,
                       th.id AS theme_id, th.name AS theme_name, th.description, th.thumbnail_url
                  FROM waiting w
                  JOIN reservation_time t ON w.time_id = t.id
                  JOIN theme th ON w.theme_id = th.id
                 WHERE w.id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    @Override
    public void updateOrderIndex(Long id, int newOrderIndex) {
        jdbcTemplate.update("UPDATE waiting SET wait_order = ? WHERE id = ?", newOrderIndex, id);
    }
}

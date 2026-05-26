package roomescape.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;

@Repository
public class WaitingDao {

    private static final RowMapper<Waiting> WAITING_ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                ThemeName.parse(rs.getString("theme_name")),
                Description.parse(rs.getString("description")),
                ThumbnailUrl.parse(rs.getString("url"))
        );
        return new Waiting(
                rs.getLong("id"),
                UserName.parse(rs.getString("name")),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                rs.getObject("create_at", LocalDateTime.class)
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", waiting.getName().value());
        params.put("date", waiting.getDate());
        params.put("time_id", waiting.getTime().getId());
        params.put("theme_id", waiting.getTheme().getId());
        params.put("created_at", waiting.getCreatedAt().toLocalDate());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Waiting(
                id,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                waiting.getCreatedAt()
        );
    }

    public void delete(Long id) {
        String sql = """
                DELETE FROM waiting 
                       WHERE id = ?
                """;

        jdbcTemplate.update(sql, id);
    }

    public boolean existsBy(Waiting waiting) {
        String sql = """
                SELECT EXISTS(
                            SELECT 1
                            FROM waiting
                            WHERE name = ? AND date = ? AND
                                  time_id = ? AND theme_id = ?
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        );

        return Boolean.TRUE.equals(result);
    }
}

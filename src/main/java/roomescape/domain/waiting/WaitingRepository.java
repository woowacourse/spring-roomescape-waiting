package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.waiting.dto.MyWaitingResult;

@Repository
public class WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<MyWaitingResult> rowMapper = (resultSet, rowNum) -> new MyWaitingResult(
            resultSet.getLong("waiting_id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            resultSet.getTime("time_start_at").toLocalTime(),
            resultSet.getString("theme_name"),
            resultSet.getInt("waiting_number")
    );

    public WaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTime().getId())
                .addValue("theme_id", waiting.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Waiting.of(id, waiting.getName(), waiting.getDate(), waiting.getTime(),
                waiting.getTheme());
    }

    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, Long timeId, Long themeId, String name) {
        String query = """
                SELECT COUNT(*)
                FROM waiting
                WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId, name);
        return count != null && count > 0;
    }

    public boolean existsById(Long id) {
        String query = """
                SELECT COUNT(*)
                FROM waiting
                WHERE id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        String query = "delete from waiting where id = ?";
        jdbcTemplate.update(query, id);
    }

    public List<MyWaitingResult> findByName(String name) {
        String query = """
                SELECT ranked.waiting_id, ranked.name, ranked.date,
                       t.start_at AS time_start_at,
                       th.name AS theme_name,
                       ranked.waiting_number
                FROM (
                    SELECT
                        w.id AS waiting_id,
                        w.name,
                        w.date,
                        w.time_id,
                        w.theme_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY w.theme_id, w.time_id, w.date
                            ORDER BY w.id
                        ) AS waiting_number
                    FROM waiting w
                ) ranked
                JOIN reservation_time t ON ranked.time_id = t.id
                JOIN theme th ON ranked.theme_id = th.id
                WHERE ranked.name = ?
                ORDER BY ranked.date DESC, ranked.waiting_id
                """;
        return jdbcTemplate.query(query, rowMapper, name);
    }
}

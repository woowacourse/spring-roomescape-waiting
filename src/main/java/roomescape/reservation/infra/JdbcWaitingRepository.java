package roomescape.reservation.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Waiting save(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getName())
                .addValue("date", waiting.getDate())
                .addValue("theme_id", waiting.getThemeId())
                .addValue("time_id", waiting.getTimeId());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return waiting.withId(id);
    }

    @Override
    public Long getRank(Waiting waiting) {
        return jdbcTemplate.queryForObject("""
                            SELECT COUNT(*)
                            FROM waiting
                            WHERE id <= ?
                              AND date = ? 
                              AND theme_id = ?
                              AND time_id = ?
                        """,
                Long.class,
                waiting.getId(),
                waiting.getDate(),
                waiting.getThemeId(),
                waiting.getTimeId());
    }
}

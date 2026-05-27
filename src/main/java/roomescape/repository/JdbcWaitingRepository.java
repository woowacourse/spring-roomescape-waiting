package roomescape.repository;

import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Waiting save(Waiting waiting) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(waiting)).longValue();

        return Waiting.of(generatedKey, waiting.getName(), waiting.getDate(),
                waiting.getTime(), waiting.getTheme(), waiting.getWaitingNumber());
    }

    @Override
    public boolean existsByNameAndDateAndTimeAndTheme(String name, LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting
                    WHERE name = :name
                      AND date = :date
                      AND time_id = :time_id
                      AND theme_id = :theme_id
                )
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }
}

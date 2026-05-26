package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Waiting save(Waiting waiting) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(waiting)).longValue();

        return Waiting.of(generatedKey, waiting.getName(), waiting.getDate(),
                waiting.getTime(), waiting.getTheme(), waiting.getWaitingNumber());
    }
}
